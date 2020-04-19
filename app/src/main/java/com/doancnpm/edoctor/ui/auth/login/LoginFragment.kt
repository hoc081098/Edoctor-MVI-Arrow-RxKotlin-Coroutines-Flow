package com.doancnpm.edoctor.ui.auth.login

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentLoginBinding
import com.doancnpm.edoctor.utils.viewBinding

class LoginFragment : BaseFragment(R.layout.fragment_login) {
  private val binding by viewBinding(FragmentLoginBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.editEmail.error = "Invalid email address"
    binding.editPassword.error = "Too short password"
    binding.signUpButton.setOnClickListener {
      findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }
  }
}