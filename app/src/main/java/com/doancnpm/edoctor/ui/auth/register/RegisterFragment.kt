package com.doancnpm.edoctor.ui.auth.register

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentRegisterBinding
import com.doancnpm.edoctor.utils.viewBinding

class RegisterFragment : BaseFragment(R.layout.fragment_register) {
  private val binding by viewBinding(FragmentRegisterBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}