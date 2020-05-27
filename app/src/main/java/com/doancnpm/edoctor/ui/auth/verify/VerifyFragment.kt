package com.doancnpm.edoctor.ui.auth.verify

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentVerifyBinding
import com.doancnpm.edoctor.utils.toast
import com.doancnpm.edoctor.utils.viewBinding

class VerifyFragment : BaseFragment(R.layout.fragment_verify) {
  private val binding by viewBinding { FragmentVerifyBinding.bind(it) }
  private val navArgs by navArgs<VerifyFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireContext().toast("Phone = ${navArgs.phone}")
  }
}