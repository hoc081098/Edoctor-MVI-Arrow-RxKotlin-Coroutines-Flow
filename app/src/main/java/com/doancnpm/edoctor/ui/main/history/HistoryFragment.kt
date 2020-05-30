package com.doancnpm.edoctor.ui.main.history

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHistoryBinding
import com.doancnpm.edoctor.utils.viewBinding

class HistoryFragment : BaseFragment(R.layout.fragment_history) {
  private val binding by viewBinding { FragmentHistoryBinding.bind(it) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}