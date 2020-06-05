package com.doancnpm.edoctor.data.repository

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import androidx.annotation.RequiresPermission
import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.toLocationDomain
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Location
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.utils.retrySuspend
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds
import android.location.Location as AndroidLocation


@ExperimentalCoroutinesApi
@ExperimentalTime
class LocationRepositoryImpl(
  private val errorMapper: ErrorMapper,
  private val application: Application
) : LocationRepository {

  private val fusedLocationClient by lazy(LazyThreadSafetyMode.NONE) {
    LocationServices.getFusedLocationProviderClient(application)
  }
  private val settingsClient by lazy(LazyThreadSafetyMode.NONE) {
    LocationServices.getSettingsClient(application)
  }
  private val locationRequest by lazy(LazyThreadSafetyMode.NONE) {
    LocationRequest()
      .setInterval(1_000)
      .setFastestInterval(500)
      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
      .setNumUpdates(1)
  }
  private val locationSettingsRequest by lazy(LazyThreadSafetyMode.NONE) {
    LocationSettingsRequest.Builder()
      .addLocationRequest(locationRequest)
      .build()
  }

  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  override suspend fun getCurrentLocation(): DomainResult<Location> {
    return Either.catch(errorMapper::map) catch@{
      try {
        settingsClient.checkLocationSettings(locationSettingsRequest).await()
      } catch (e: ResolvableApiException) {
        throw AppError.LocationError.LocationSettingsDisabled(e)
      }

      fusedLocationClient
        .lastLocation
        .await()
        .also { Timber.d("lastLocation: $it") }
        ?.run { return@catch toLocationDomain() }

      try {
        retrySuspend(
          times = 3,
          initialDelay = 500.milliseconds,
          factor = 2.0
        ) {
          withTimeout(5.seconds) {
            Timber.d("[requestLocationUpdates]: started times: $it")
            requestLocationUpdates()
          }
        }.toLocationDomain()
      } catch (e: TimeoutCancellationException) {
        throw AppError.LocationError.TimeoutGetCurrentLocation
      }
    }
  }

  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  private suspend fun requestLocationUpdates(): AndroidLocation {
    return suspendCancellableCoroutine { continuation ->

      val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
          (locationResult?.lastLocation ?: return)
            .also { Timber.d("[locationCallback] resume: $it") }
            .let {
              continuation.resume(it) {
                fusedLocationClient.removeLocationUpdates(this)
                Timber.d("[locationCallback] remove")
              }
            }
        }
      }

      continuation.invokeOnCancellation {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Timber.d("[invokeOnCancellation]")
      }

      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        null,
      )
    }
  }
}
