package com.doancnpm.edoctor.ui.main.home.create_order

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.utils.asLiveData
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

data class Location(val lat: Double, val lng: Double)

class CreateOrderVM(private val application: Application) : BaseVM() {
  private val fusedLocationClient by lazy(NONE) {
    LocationServices.getFusedLocationProviderClient(application)
  }

  private val locationD = MutableLiveData<Location>()

  val canGoNextLiveDatas = listOf(
    locationD.map { it !== null },
    locationD.map { it !== null },
    locationD.map { it !== null },
    locationD.map { it !== null },
  )
  val locationLiveData get() = locationD.asLiveData()

  @SuppressLint("MissingPermission")
  fun getCurrentLocation() {
    viewModelScope.launch {
      fusedLocationClient.lastLocation
        .await()
        .also { Timber.d("lastLocation: $it") }
        ?.let {
          locationD.value = Location(
            lat = it.latitude,
            lng = it.longitude
          )
          return@launch
        }
    }
  }


}