package com.doancnpm.edoctor.ui.main.home.services

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentServicesBinding
import com.doancnpm.edoctor.utils.viewBinding

class ServicesFragment : BaseFragment(R.layout.fragment_services) {
  private val binding by viewBinding { FragmentServicesBinding.bind(it) }
  private val navArgs by navArgs<ServicesFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
    navArgs.category
  }
}