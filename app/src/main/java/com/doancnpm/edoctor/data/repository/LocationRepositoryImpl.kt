package com.doancnpm.edoctor.data.repository

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import androidx.annotation.RequiresPermission
import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.GeocoderApiService
import com.doancnpm.edoctor.data.remote.response.GeocoderErrorResponseJsonAdapter
import com.doancnpm.edoctor.data.toLocationDomain
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Location
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.utils.retrySuspend
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds
import android.location.Location as AndroidLocation


@ExperimentalCoroutinesApi
@ExperimentalTime
class LocationRepositoryImpl(
  private val errorMapper: ErrorMapper,
  private val application: Application,
  private val geocoderApiService: GeocoderApiService,
  private val geocoderApiKey: String,
  private val geocoderErrorResponseJsonAdapter: GeocoderErrorResponseJsonAdapter,
  private val dispatchers: AppDispatchers,
) : LocationRepository {

  private val fusedLocationClient by lazy {
    LocationServices.getFusedLocationProviderClient(application)
  }
  private val settingsClient by lazy {
    LocationServices.getSettingsClient(application)
  }
  private val locationRequest by lazy {
    LocationRequest()
      .setInterval(1_000)
      .setFastestInterval(500)
      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
      .setNumUpdates(1)
  }
  private val locationSettingsRequest by lazy {
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

  override suspend fun getAddressForCoordinates(location: Location): DomainResult<String> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        val response = geocoderApiService
          .getAddressForCoordinates(
            latlng = "${location.latitude},${location.longitude}",
            key = geocoderApiKey
          )

        if (!response.isSuccessful) {
          response
            .errorBody()!!
            .use { geocoderErrorResponseJsonAdapter.fromJson(it.toString())!! }
            .let {
              throw AppError.Remote.ServerError(
                errorMessage = it.errorMessage,
                statusCode = response.code(),
                cause = null,
              )
            }
        }

        (response
          .body()!!
          .results
          .firstOrNull() ?: throw AppError.LocationError.GeocoderEmptyResult)
          .formattedAddress
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
