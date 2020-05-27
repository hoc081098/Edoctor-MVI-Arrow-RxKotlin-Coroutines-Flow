package com.doancnpm.edoctor.ui.auth.resendcode

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentResendCodeBinding
import com.doancnpm.edoctor.utils.viewBinding

class ResendCodeFragment : BaseFragment(R.layout.fragment_resend_code) {
  private val binding by viewBinding { FragmentResendCodeBinding.bind(it) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonGetOtp.setOnClickListener {
      ResendCodeFragmentDirections
        .actionResendCodeFragmentToVerifyFragment("+84363438135")
        .let { findNavController().navigate(it) }
    }

    binding.textView2.text = HtmlCompat.fromHtml(
      "We will send you a <b>One time code</b> on your phone number",
      FROM_HTML_MODE_LEGACY
    )
  }
}