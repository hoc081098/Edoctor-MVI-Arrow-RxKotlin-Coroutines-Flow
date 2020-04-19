package com.doancnpm.edoctor.ui.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentLoginBinding

class LoginFragment : BaseFragment() {
  private lateinit var binding: FragmentLoginBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ) = FragmentLoginBinding.inflate(
    inflater,
    container,
    false
  ).also { binding = it }.root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.editEmail.error = "Invalid email address"
    binding.editPassword.error = "Too short password"
    binding.signUpButton.setOnClickListener {
      findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }
  }
}