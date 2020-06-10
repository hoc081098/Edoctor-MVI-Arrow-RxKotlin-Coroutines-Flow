package com.doancnpm.edoctor.ui.main.home.create_order

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.*
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.asObservable
import com.doancnpm.edoctor.utils.setValueNotNull
import com.doancnpm.edoctor.utils.toObservable
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class CreateOrderVM(private val locationRepository: LocationRepository) : BaseVM() {

  private val singleEventS = PublishRelay.create<SingleEvent>()

  private val locationD = MutableLiveData<Location>()
  private val timesD = MutableLiveData<Times>().apply { value = Times() }

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
    Observable.just(false),
    Observable.just(false),
  )

  val locationLiveData get() = locationD.asLiveData()
  val timesLiveData get() = timesD.asLiveData()
  val singleEventObservable get() = singleEventS.asObservable()

  init {
    canGoNextObservables.forEachIndexed { index, observable ->
      observable
        .subscribeBy { Timber.d("[Can go next] $index -> $it") }
        .addTo(compositeDisposable)
    }
  }

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
}