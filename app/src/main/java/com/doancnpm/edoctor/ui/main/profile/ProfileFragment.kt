package com.doancnpm.edoctor.ui.main.profile

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentProfileBinding
import com.doancnpm.edoctor.utils.viewBinding

class ProfileFragment : BaseFragment(R.layout.fragment_profile) {
  private val binding by viewBinding { FragmentProfileBinding.bind(it) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}