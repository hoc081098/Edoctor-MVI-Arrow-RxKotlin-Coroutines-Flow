package com.doancnpm.edoctor.ui.main.home.create_order

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.domain.repository.PromotionRepository
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.*
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.InputPromotionContract.PromotionItem
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.InputPromotionContract.ViewState
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.asObservable
import com.doancnpm.edoctor.utils.setValueNotNull
import com.doancnpm.edoctor.utils.toObservable
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.LazyThreadSafetyMode.NONE

class CreateOrderVM(
  val service: Service,
  private val locationRepository: LocationRepository,
  promotionRepository: PromotionRepository,
) : BaseVM() {
  private val inputPromotionVM by lazy(NONE) {
    InputPromotionVM(
      promotionRepository = promotionRepository,
      viewModelScope = viewModelScope
    )
  }

  private val singleEventS = PublishRelay.create<SingleEvent>()

  private val locationD = MutableLiveData<Location>()
  private val timesD = MutableLiveData<Times>().apply { value = Times() }
  private val noteD = MutableLiveData<String?>()

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
    Observable.just(true), // confirmation
  )

  val locationLiveData get() = locationD.asLiveData()
  val timesLiveData get() = timesD.asLiveData()
  val noteLiveData get() = noteD.asLiveData()
  val promotionItemLiveData get() = inputPromotionVM.selectedItem
  val singleEventObservable get() = singleEventS.asObservable()

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
          ifLeft = { singleEventS.accept(SingleEvent.Error(it)) },
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

  fun retry() = inputPromotionVM.retry()

  fun select(item: PromotionItem) = inputPromotionVM.select(item)
  //endregion

  fun setNote(note: String?) {
    Timber.d("Set note=$note")
    noteD.value = note
  }
}

private class InputPromotionVM(
  private val promotionRepository: PromotionRepository,
  private val viewModelScope: CoroutineScope
) {
  private val selectedItemD = MutableLiveData<PromotionItem>()

  private val viewStateD by lazy(NONE) {
    MutableLiveData<ViewState>()
      .apply {
        value =
          ViewState()
      }
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

  fun select(item: PromotionItem) {
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
    viewModelScope.launch {
      viewStateD.setValueNotNull { it.copy(isLoading = true, error = null) }

      promotionRepository
        .getPromotions()
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
                  PromotionItem(
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