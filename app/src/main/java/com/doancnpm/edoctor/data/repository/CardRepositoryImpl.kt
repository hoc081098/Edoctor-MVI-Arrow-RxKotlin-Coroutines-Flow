package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.AddCardBody
import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.repository.CardRepository

class CardRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
) : CardRepository {
  override suspend fun addCard(card: Card): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      apiService
        .addCard(
          AddCardBody(
            cardHolderName = card.holderName,
            number = card.number,
            expMonth = card.expiredMonth,
            expYear = card.expiredYear,
            cvc = card.cvc,
          )
        )
        .unwrap()
    }
  }
}