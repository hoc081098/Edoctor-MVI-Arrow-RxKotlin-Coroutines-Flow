package com.doancnpm.edoctor.ui.main.home.create_order.select_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.CardRepository
import com.doancnpm.edoctor.ui.main.home.create_order.select_card.SelectCardContract.ViewState
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.flatMapFirst
import com.doancnpm.edoctor.utils.setValueNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber

@ExperimentalCoroutinesApi
class SelectCardVM(
  private val cardRepository: CardRepository,
) : BaseVM() {
  private val stateD = MutableLiveData<ViewState>().apply { value = ViewState() }
  private val fetchChannel = Channel<Unit>(Channel.BUFFERED)

  val viewState get() = stateD.asLiveData()

  fun retry() {
    if (stateD.value!!.error !== null) {
      fetchChannel.offer(Unit)
    }
  }

  init {
    fetchChannel
      .consumeAsFlow()
      .onStart { emit(Unit) }
      .flatMapFirst {
        cardRepository
          .getCards()
          .onStart {
            stateD.setValueNotNull {
              it.copy(
                isLoading = true,
                error = null
              )
            }
          }
      }
      .onEach { result ->
        result.fold(
          ifLeft = { error ->
            stateD.setValueNotNull {
              it.copy(
                isLoading = false,
                error = error
              )
            }
          },
          ifRight = { cards ->
            stateD.setValueNotNull {
              it.copy(
                isLoading = false,
                cards = cards
              )
            }
          }
        )
      }
      .catch { Timber.d(it, "Unhandled exception: $it") }
      .launchIn(viewModelScope)
  }
}