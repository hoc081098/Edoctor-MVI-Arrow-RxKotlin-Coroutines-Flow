package com.doancnpm.edoctor.ui.main.home.service_detail

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentServiceDetailBinding
import com.doancnpm.edoctor.utils.currencyVndFormatted
import com.doancnpm.edoctor.utils.toast
import com.doancnpm.edoctor.utils.viewBinding

class ServiceDetailFragment : BaseFragment(R.layout.fragment_service_detail) {
  private val binding by viewBinding(
    {
      buttonOrder.setOnClickListener(null)
    }
  ) { FragmentServiceDetailBinding.bind(it) }
  private val navArgs by navArgs<ServiceDetailFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val service = navArgs.service

    GlideApp
      .with(this)
      .load(service.image)
      .placeholder(R.drawable.logo)
      .centerCrop()
      .transition(withCrossFade())
      .into(binding.appBarImage)

    binding.textDescription.text = service.description
    binding.buttonOrder.text = "ORDER - ${service.price.currencyVndFormatted}"
    binding.buttonOrder.setOnClickListener {
      it.context.toast("Click order")
    }
  }
}