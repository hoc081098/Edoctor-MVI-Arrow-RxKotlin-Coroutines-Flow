package com.doancnpm.edoctor.ui.main.home.create_order.inputs.address

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputAddressBinding
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.coroutines.resume

class InputAddressFragment : BaseFragment(R.layout.fragment_input_address) {
  private val binding by viewBinding<FragmentInputAddressBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  private var _googleMap: GoogleMap? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
  }

  private fun setupViews() {
    (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).run {
      if (_googleMap === null) {
        getMapAsync {
          _googleMap = it

          // Add a marker in Sydney and move the camera
          val sydney = LatLng(-34.0, 151.0)
          it.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
          it.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
      }
    }

    binding.currentLocationFab.setOnClickListener {
      lifecycleScope.launch {
        if (requestLocationPermission()) {
          requireActivity().toast("Good!")
        } else {
          requireActivity().toast("Bad!")
        }
      }
    }
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

    if (shouldShowRequestPermissionRationale(permission)) {
      requireView().snack(permission) {

        continuation.invokeOnCancellation {
          dismiss()
          requestPermission.unregister()
        }

        var clickedOK = false
        action("OK") {
          clickedOK = true
          requestPermission.launch(permission)
        }

        onDismissed {
          if (!clickedOK && continuation.isActive) {
            continuation.resume(false)
          }
        }
      }

    } else {
      continuation.invokeOnCancellation {
        requestPermission.unregister()
      }

      requestPermission.launch(permission)
    }
  }
}