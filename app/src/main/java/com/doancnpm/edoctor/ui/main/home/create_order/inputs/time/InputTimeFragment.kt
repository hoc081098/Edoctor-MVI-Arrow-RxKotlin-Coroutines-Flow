package com.doancnpm.edoctor.ui.main.home.create_order.inputs.time

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputTimeBinding
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.pickDateObservable
import com.doancnpm.edoctor.utils.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.*
import kotlin.LazyThreadSafetyMode.NONE

class InputTimeFragment : BaseFragment(R.layout.fragment_input_time) {
  private val binding by viewBinding<FragmentInputTimeBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    binding.buttonStartTime.setOnClickListener {
      lifecycleScope.launch {
        val initial = viewModel.timesLiveData.value!!.startTime
        val date = requireActivity().pickDateObservable(initial).await()
        if (date != null && date != initial) {
          viewModel.setStartDate(date)
        }
      }
    }
  }

  private fun setupViews() {

  }
}