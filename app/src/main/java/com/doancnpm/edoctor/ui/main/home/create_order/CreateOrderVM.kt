package com.doancnpm.edoctor.ui.main.home.create_order

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.Promotion
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.domain.repository.CardRepository
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.domain.repository.PromotionRepository
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalCoroutinesApi
class CreateOrderVM(
  val service: Service,
  private val locationRepository: LocationRepository,
  private val cardRepository: CardRepository,
  private val orderRepository: OrderRepository,
  promotionRepository: PromotionRepository,
) : BaseVM() {
  private val inputPromotionVM by lazy(NONE) {
    InputPromotionVM(
      promotionRepository = promotionRepository,
      viewModelScope = viewModelScope
    )
  }

  private val selectCardVM by lazy(NONE) {
    SelectCardVM(
      cardRepository = cardRepository,
      viewModelScope = viewModelScope,
    )
  }

  private val singleEventS = PublishRelay.create<SingleEvent>()
  private val isSubmittingD = MutableLiveData<Boolean>().apply { value = false }

  private val locationD = MutableLiveData<Location>()
  private val timesD = MutableLiveData<Times>().apply { value = Times() }
  private val noteD = MutableLiveData<String?>().apply { value = null }

  val canGoNextObservables: List<Observable<Boolean>> = listOf(
    locationD
      .map { it != null }
      .toObservable { false }
      .replay(1)
      .refCount(),
    timesD
      .map { it.startTime !== null && it.endTime !== null }
      .toObservable { false }
      .replay(1)
      .refCount(),
    Observable.just(true), // promotion (optional)
    Observable.just(true), // note (optional),
    Observable
      .defer {
        selectCardVM
          .selected
          .map { it !== null }
          .toObservable { false }
      }
      .replay(1)
      .refCount(), // card (required),
    Observable.just(true), // confirmation
  )

  val locationLiveData get() = locationD.asLiveData()
  val timesLiveData get() = timesD.asLiveData()
  val noteLiveData get() = noteD.asLiveData()
  val promotionItemLiveData get() = inputPromotionVM.selectedItem
  private val cardLiveData get() = selectCardVM.selected

  val singleEventObservable get() = singleEventS.asObservable()
  val isSubmittingLiveData get() = isSubmittingD.asLiveData()

  init {
    canGoNextObservables.forEachIndexed { index, observable ->
      observable
        .subscribeBy { Timber.d("[Can go next] $index -> $it") }
        .addTo(compositeDisposable)
    }
  }

  //region Location
  @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
  fun getCurrentLocation() {
    viewModelScope.launch {
      locationRepository
        .getCurrentLocation()
        .fold(
          ifLeft = {
            singleEventS.accept(
              if (it is AppError.LocationError) {
                SingleEvent.AddressError(it)
              } else {
                SingleEvent.Error(it)
              }
            )
          },
          ifRight = {
            locationD.value = Location(
              lat = it.latitude,
              lng = it.longitude
            )
          }
        )
    }
  }

  fun setLocation(location: Location) {
    locationD.value = location
  }
  //endregion

  //region Times
  fun setStartDate(date: Date) {
    timesD.setValueNotNull { times ->
      when (val endTime = times.endTime) {
        null -> if (date <= Date()) {
          singleEventS.accept(SingleEvent.Times.PastStartTime)
          times
        } else {
          times.copy(startTime = date)
        }
        else -> if (endTime <= date) {
          singleEventS.accept(SingleEvent.Times.StartTimeIsLaterThanEndTime)
          times
        } else {
          times.copy(startTime = date)
        }
      }
    }
  }

  fun setEndDate(date: Date) {
    timesD.setValueNotNull { times ->
      when (val startTime = times.startTime) {
        null -> if (date <= Date()) {
          singleEventS.accept(SingleEvent.Times.PastEndTime)
          times
        } else {
          times.copy(endTime = date)
        }
        else -> if (startTime >= date) {
          singleEventS.accept(SingleEvent.Times.EndTimeIsEarlierThanStartTime)
          times
        } else {
          times.copy(endTime = date)
        }
      }
    }
  }

  fun clearTimes() {
    timesD.setValueNotNull { it.copy(startTime = null, endTime = null) }
  }
  //endregion

  //region Promotion
  val promotionViewState get() = inputPromotionVM.viewState

  fun retryGetPromotion() = inputPromotionVM.retry()

  fun select(item: InputPromotionContract.PromotionItem) = inputPromotionVM.select(item)
  //endregion

  fun setNote(note: String?) {
    Timber.d("Set note=$note")
    noteD.value = note
  }

  //region Cards
  val selectCardViewState
    get() = selectCardVM.viewState

  fun retryGetCards() = selectCardVM.retry()

  fun select(item: SelectCardContract.CardItem) = selectCardVM.select(item)
  //endregion

  fun submit() {
    if (isSubmittingD.value!!) return
    isSubmittingD.value = true

    viewModelScope.launch {
      val location: com.doancnpm.edoctor.domain.entity.Location
      val note: String?
      val promotion: Promotion?
      val card: Card
      val startTime: Date
      val endTime: Date

      try {
        location = locationLiveData.value!!.toDomain()
        note = noteLiveData.value
        promotion = promotionItemLiveData.value?.promotion
        card = cardLiveData.value!!
        startTime = timesLiveData.value!!.startTime!!
        endTime = timesLiveData.value!!.endTime!!
      } catch (e: KotlinNullPointerException) {
        singleEventS.accept(SingleEvent.MissingRequiredInput)
        isSubmittingD.value = false
        return@launch
      }

      orderRepository
        .createOrder(
          service = service,
          location = location,
          note = note,
          promotion = promotion,
          card = card,
          startTime = startTime,
          endTime = endTime,
        )
        .also { isSubmittingD.value = false }
        .fold(
          ifLeft = { SingleEvent.Error(it) },
          ifRight = { SingleEvent.Success }
        )
        .let { singleEventS.accept(it) }
    }
  }
}

private class InputPromotionVM(
  private val promotionRepository: PromotionRepository,
  private val viewModelScope: CoroutineScope
) {
  private val selectedItemD = MutableLiveData<InputPromotionContract.PromotionItem?>().apply { value = null }

  private var isLoading = false
  private val viewStateD by lazy(NONE) {
    MutableLiveData<InputPromotionContract.ViewState>()
      .apply { value = InputPromotionContract.ViewState() }
      .also { fetchPromotion() }
  }

  val viewState get() = viewStateD.asLiveData()
  val selectedItem get() = selectedItemD.asLiveData()

  fun retry() {
    val value = viewStateD.value!!
    if (value.error !== null && !value.isLoading) {
      fetchPromotion()
    }
  }

  fun select(item: InputPromotionContract.PromotionItem) {
    val old = selectedItemD.value
    selectedItemD.value = if (old === null || old.promotion.id != item.promotion.id) item else null

    viewStateD.setValueNotNull { state ->
      state.copy(
        isLoading = false,
        error = null,
        promotions = state.promotions.map {
          it.copy(
            isSelected = it.promotion.id == selectedItemD.value?.promotion?.id
          )
        }
      )
    }
  }

  private fun fetchPromotion() {
    if (isLoading) return
    isLoading = true

    viewModelScope.launch {
      viewStateD.setValueNotNull { it.copy(isLoading = true, error = null) }

      promotionRepository
        .getPromotions()
        .also { isLoading = false }
        .fold(
          ifLeft = { error ->
            Timber.d(error, "Error: $error")
            viewStateD.setValueNotNull { it.copy(isLoading = false, error = error) }
          },
          ifRight = { promotions ->
            viewStateD.setValueNotNull { state ->
              state.copy(
                isLoading = false,
                error = null,
                promotions = promotions.map {
                  InputPromotionContract.PromotionItem(
                    promotion = it,
                    isSelected = selectedItemD.value?.promotion?.id == it.id,
                  )
                }
              )
            }
          }
        )
    }
  }
}

@ExperimentalCoroutinesApi
private class SelectCardVM(
  private val cardRepository: CardRepository,
  private val viewModelScope: CoroutineScope,
) {
  private var selectedD = MutableLiveData<Card?>().apply { value = null }
  private val stateD = MutableLiveData<SelectCardContract.ViewState>().apply { value = SelectCardContract.ViewState() }
  private val fetchChannel = Channel<Unit>(Channel.BUFFERED)

  val viewState get() = stateD.asLiveData()
  val selected get() = selectedD.asLiveData()

  fun retry() {
    if (stateD.value!!.error !== null) {
      fetchChannel.offer(Unit)
    }
  }

  fun select(item: SelectCardContract.CardItem) {
    selectedD.value = item.card

    stateD.setValueNotNull { state ->
      state.copy(
        isLoading = false,
        items = state.items.map {
          it.copy(isSelected = it.card.id == selectedD.value?.id)
        }
      )
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
            stateD.setValueNotNull { state ->
              state.copy(
                isLoading = false,
                items = cards.map {
                  SelectCardContract.CardItem(
                    card = it,
                    isSelected = it.id == selectedD.value?.id,
                  )
                }
              )
            }
          }
        )
      }
      .catch { Timber.d(it, "Unhandled exception: $it") }
      .launchIn(viewModelScope)
  }
}