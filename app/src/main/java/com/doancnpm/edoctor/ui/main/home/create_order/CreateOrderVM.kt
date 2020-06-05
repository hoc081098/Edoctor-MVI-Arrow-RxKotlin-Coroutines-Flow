package com.doancnpm.edoctor.ui.main.home.create_order

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.Location
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.SingleEvent
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.toObservable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateOrderVM(private val locationRepository: LocationRepository) : BaseVM() {

  private val singleEventD = MutableLiveData<Event<SingleEvent>>()
  private val locationD = MutableLiveData<Location>()

  val canGoNextObservables: List<Observable<Boolean>> = listOf(
    locationD
      .map { it != null }
      .toObservable { false }
      .replay(1)
      .refCount(),
    Observable.just(false),
    Observable.just(false),
    Observable.just(false),
  )

  val locationLiveData get() = locationD.asLiveData()
  val singleEventLiveData get() = singleEventD.asLiveData()

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
          ifLeft = { singleEventD.value = Event(SingleEvent.Error(it)) },
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
}