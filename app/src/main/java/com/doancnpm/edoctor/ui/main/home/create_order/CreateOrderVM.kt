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
import kotlinx.coroutines.launch

class CreateOrderVM(private val locationRepository: LocationRepository) : BaseVM() {

  private val singleEventD = MutableLiveData<Event<SingleEvent>>()
  private val locationD = MutableLiveData<Location>()

  val canGoNextLiveDatas = listOf(
    locationD.map { it !== null },
    locationD.map { it !== null },
    locationD.map { it !== null },
    locationD.map { it !== null },
  )

  val locationLiveData get() = locationD.asLiveData()
  val singleEventLiveData get() = singleEventD.asLiveData()

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
}