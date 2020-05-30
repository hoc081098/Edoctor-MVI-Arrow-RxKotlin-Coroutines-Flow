package com.doancnpm.edoctor.ui.main.notifications

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentNotificationsBinding
import com.doancnpm.edoctor.utils.viewBinding

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
  private val binding by viewBinding { FragmentNotificationsBinding.bind(it) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}