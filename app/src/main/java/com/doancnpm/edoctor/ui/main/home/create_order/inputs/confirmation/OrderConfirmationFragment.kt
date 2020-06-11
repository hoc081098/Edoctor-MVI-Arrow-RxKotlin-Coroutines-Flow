package com.doancnpm.edoctor.ui.main.home.create_order.inputs.confirmation

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doancnpm.edoctor.BuildConfig
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentOrderConfirmationBinding
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.math.roundToInt

class OrderConfirmationFragment : BaseFragment(R.layout.fragment_order_confirmation) {
  private val binding by viewBinding<FragmentOrderConfirmationBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val glide = GlideApp.with(this@OrderConfirmationFragment)

    binding.service.run {
      orderButton.gone()

      val item = viewModel.service
      glide
        .load(item.image)
        .placeholder(R.drawable.logo)
        .thumbnail(0.5f)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)

      textName.text = item.name
      textPrice.text = item.price.currencyVndFormatted
      textDescription.text = item.description
    }

    binding.run {

      viewModel.timesLiveData.observe(owner = viewLifecycleOwner) { times ->
        textStartTime.text = times.startTime!!.toString_yyyyMMdd_HHmmss()
        textEndTime.text = times.endTime!!.toString_yyyyMMdd_HHmmss()
      }

      viewModel.locationLiveData.observe(owner = viewLifecycleOwner) { location ->
        val numberFormat = DecimalFormat("#.##")
        textAddress.text =
          "Latitude and longitude: ${numberFormat.format(location.lat)}, ${numberFormat.format(
            location.lng
          )}"

        val url =
          "https://api.mapbox.com/styles/v1/mapbox/light-v10/static/${location.lng},${location.lat},15,0/128x128?access_token=${BuildConfig.MAPBOX_ACCESS_TOKEN}"
        glide
          .load(url)
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(imageAddress)
      }

      textNote.run {
        maxLines = Int.MAX_VALUE
        viewModel.noteLiveData.observe(viewLifecycleOwner, Observer { text = it })
      }

      viewModel.promotionItemLiveData.observe(viewLifecycleOwner, Observer { item ->
        textDiscount.text = item
          ?.promotion
          ?.let { promotion -> "${promotion.name} - ${(promotion.discount * 100).roundToInt()}% OFF" }

        textPrice.text = (item
          ?.promotion
          ?.let { viewModel.service.price * (1 - it.discount) }
          ?: viewModel.service.price.toDouble())
          .also { Timber.d("Price: $it") }
          .currencyVndFormatted
      })
    }
  }
}