package com.doancnpm.edoctor.ui.main.home.create_order.inputs.address

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputAddressBinding
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.Location
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.*
import com.doancnpm.edoctor.utils.SnackbarDismissEvent.DISMISS_EVENT_ACTION
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.coroutines.resume

class InputAddressFragment : BaseFragment(R.layout.fragment_input_address) {
  private val binding by viewBinding<FragmentInputAddressBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  private var _googleMap: GoogleMap? = null
  private var marker: Marker? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _googleMap?.setOnMapLongClickListener(null)
    _googleMap = null
  }

  @SuppressLint("MissingPermission") // Already ensured permission
  private fun bindVM() {
    viewModel.locationLiveData.observe(owner = viewLifecycleOwner) {
      moveCameraToLocation(it)
    }
    viewModel.singleEventLiveData.observeEvent(owner = viewLifecycleOwner) { event ->
      when (event) {
        is CreateOrderContract.SingleEvent.Error -> {
          val error = event.appError
          view?.snack(error.getMessage())

          if (error is AppError.LocationError.LocationSettingsDisabled
            && error.throwable is ResolvableApiException
          ) {
            error.throwable
            error.throwable.startResolutionForResult(
              requireActivity(),
              REQUEST_CHECK_SETTINGS
            )
          }
        }
      }
    }

    binding.currentLocationFab.setOnClickListener {
      lifecycleScope.launch {
        if (requestLocationPermission()) {
          viewModel.getCurrentLocation()
        } else {
          requireActivity().toast("You need grant location permission to retrieve current location!")
        }
      }
    }
  }

  private fun moveCameraToLocation(location: Location) {
    _googleMap?.run {
      val latLng = LatLng(location.lat, location.lng)

      marker?.remove()
      marker = MarkerOptions()
        .position(latLng)
        .title("Picked location")
        .apply {
          requireContext()
            .getDrawableBy(id = R.drawable.ic_baseline_location_on_24)
            ?.toBitmap()
            ?.let { icon(BitmapDescriptorFactory.fromBitmap(it)) }
        }
        .let(::addMarker)

      CameraUpdateFactory.newLatLngZoom(latLng, 17f).let(::moveCamera)
    }
  }

  private fun setupViews() {
    (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).run {
      if (_googleMap === null) {
        getMapAsync { map ->
          _googleMap = map
          viewModel.locationLiveData.value?.let(::moveCameraToLocation)

          map.uiSettings.run {
            isCompassEnabled = true
            isZoomControlsEnabled = true
          }
          map.setOnMapLongClickListener { latLng ->
            requireActivity().showAlertDialog {
              title("Select clicked address")
              message("Do you want to select the recently clicked address on the map?")

              negativeAction("Cancel") { _, _ -> }
              positiveAction("OK") { _, _ ->
                viewModel.setLocation(
                  Location(
                    lat = latLng.latitude,
                    lng = latLng.longitude
                  )
                )
              }
            }
          }
        }
      }
    }

    (childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment).run {
      setPlaceFields(
        listOf(
          Field.LAT_LNG,
          Field.NAME,
          Field.ADDRESS
        )
      )

      setOnPlaceSelectedListener(object : PlaceSelectionListener {
        override fun onPlaceSelected(place: Place) {
          val latLng = place.latLng
            ?: return context?.toast("Cannot get latitude and longitude").unit

          viewModel.setLocation(
            Location(
              lat = latLng.latitude,
              lng = latLng.longitude,
            )
          )
        }

        override fun onError(status: Status) {
          if (status != Status.RESULT_CANCELED) {
            context?.toast("Error when getting result")
          }
        }
      })
    }
  }

  @SuppressLint("MissingPermission")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
      Timber.d("Check settings success")
      viewModel.getCurrentLocation()
    }
  }

  private companion object {
    const val REQUEST_CHECK_SETTINGS = 1
  }
}

suspend fun Fragment.requestLocationPermission(): Boolean {
  val list = arrayOf(
    ACCESS_FINE_LOCATION,
    ACCESS_COARSE_LOCATION,
  ).map { it to (ContextCompat.checkSelfPermission(requireContext(), it) == PERMISSION_GRANTED) }

  val (permission) = list.find { !it.second } ?: return true

  return suspendCancellableCoroutine { continuation ->
    val requestPermission = registerForActivityResult(RequestPermission()) { isGranted ->
      if (continuation.isActive) {
        continuation.resume(isGranted)
      }
    }

    when {
      shouldShowRequestPermissionRationale(permission) -> {
        requireView().snack("You need grant location permission to retrieve current location!") {

          action("OK") { requestPermission.launch(permission) }

          val onDismissed = onDismissed {
            if (it != DISMISS_EVENT_ACTION && continuation.isActive) {
              continuation.resume(false)
            }
          }

          continuation.invokeOnCancellation {
            removeCallback(onDismissed)
            dismiss()
            requestPermission.unregister()
          }
        }
      }
      else -> {
        continuation.invokeOnCancellation { requestPermission.unregister() }
        requestPermission.launch(permission)
      }
    }
  }
}