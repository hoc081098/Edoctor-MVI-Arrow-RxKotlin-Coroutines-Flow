package com.doancnpm.edoctor.ui.main.home.service_detail

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentServiceDetailBinding
import com.doancnpm.edoctor.utils.viewBinding

class ServiceDetailFragment : BaseFragment(R.layout.fragment_service_detail) {
  private val binding by viewBinding { FragmentServiceDetailBinding.bind(it) }
  private val navArgs by navArgs<ServiceDetailFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
    navArgs.title
    navArgs.service
  }
}