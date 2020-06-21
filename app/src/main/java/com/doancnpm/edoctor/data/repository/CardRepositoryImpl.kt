package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.AddCardBody
import com.doancnpm.edoctor.data.toCardDomain
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.rightResult
import com.doancnpm.edoctor.domain.repository.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class CardRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
) : CardRepository {
  private val changeChannel = BroadcastChannel<Change>(Channel.BUFFERED)

  sealed class Change {
    data class Added(val card: Card) : Change()
    data class Removed(val id: String) : Change()
  }

  override suspend fun addCard(holderName: String, number: String, expiredMonth: Int, expiredYear: Int, cvc: Int): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      apiService
        .addCard(
          AddCardBody(
            cardHolderName = holderName,
            number = number,
            expMonth = expiredMonth,
            expYear = expiredYear,
            cvc = cvc,
          )
        )
        .unwrap()
        .let { changeChannel.send(Change.Added(it.toCardDomain())) }
    }
  }

  override suspend fun removeCard(id: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      apiService.removeCard(id).unwrap()
      changeChannel.send(Change.Removed(id))
    }
  }

  override fun getCards(): Flow<DomainResult<List<Card>>> {
    return flow {
      val initial = apiService.getCards()
        .unwrap()
        .map { it.toCardDomain() }

      changeChannel
        .asFlow()
        .scan(initial) { acc, change ->
          when (change) {
            is Change.Added -> acc + change.card
            is Change.Removed -> acc.filter { it.id != change.id }
          }
        }
        .let { emitAll(it) }
    }
      .map { it.rightResult() }
      .catch { throwable ->
        errorMapper
          .mapAsLeft<List<Card>>(throwable)
          .let { emit(it) }
      }
      .flowOn(dispatchers.io)
  }
}
