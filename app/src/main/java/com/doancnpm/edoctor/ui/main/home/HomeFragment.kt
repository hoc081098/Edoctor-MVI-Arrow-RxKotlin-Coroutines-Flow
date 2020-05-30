package com.doancnpm.edoctor.ui.main.home

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHomeBinding
import com.doancnpm.edoctor.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment(R.layout.fragment_home) {
  private val binding by viewBinding { FragmentHomeBinding.bind(it) }
  private val viewModel by viewModel<HomeVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.toString()
  }
}