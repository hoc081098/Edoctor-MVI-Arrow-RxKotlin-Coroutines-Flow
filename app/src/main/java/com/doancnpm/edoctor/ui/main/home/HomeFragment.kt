package com.doancnpm.edoctor.ui.main.home

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHomeBinding
import com.doancnpm.edoctor.utils.viewBinding

class HomeFragment : BaseFragment(R.layout.fragment_home) {
  private val binding by viewBinding { FragmentHomeBinding.bind(it) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}