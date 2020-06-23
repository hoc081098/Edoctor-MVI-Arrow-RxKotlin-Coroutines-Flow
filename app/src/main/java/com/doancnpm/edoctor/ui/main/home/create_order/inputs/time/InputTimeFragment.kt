package com.doancnpm.edoctor.ui.main.home.create_order.inputs.time

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputTimeBinding
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.SingleEvent
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.*
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalCoroutinesApi
class InputTimeFragment : BaseFragment(R.layout.fragment_input_time) {
  private val binding by viewBinding<FragmentInputTimeBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.timesLiveData.observe(owner = viewLifecycleOwner) { times ->
      binding.run {
        buttonStartTime.text = times.startTime?.toString_yyyyMMdd_HHmmss()
          ?: "Not selected"
        buttonEndTime.text = times.endTime?.toString_yyyyMMdd_HHmmss()
          ?: "Not selected"
      }
    }
    viewModel.singleEventObservable.subscribeBy onNext@{ event ->
      view?.snack(
        when (event) {
          SingleEvent.Times.PastStartTime -> "The start time is over. Pick again!"
          SingleEvent.Times.StartTimeIsLaterThanEndTime -> "Start time is later than end time. Pick again!"
          SingleEvent.Times.PastEndTime -> "The end time is over. Pick again!"
          SingleEvent.Times.EndTimeIsEarlierThanStartTime -> "The end time is earlier than start time. Pick again!"
          else -> return@onNext
        }
      )
    }.addTo(compositeDisposable)

    val onClickListener = View.OnClickListener listener@{ button ->
      if (button === binding.buttonClear) {
        return@listener viewModel.clearTimes()
      }

      val isStart = button === binding.buttonStartTime

      lifecycleScope.launch {
        val initial = viewModel.timesLiveData.value!!.run { if (isStart) startTime else endTime }

        val date = requireActivity().pickDateObservable(initial).await() ?: return@launch

        val initialCalendar = Calendar.getInstance().apply { time = initial ?: Date() }
        val (hourOfDay, minute) = requireContext()
          .pickTimeObservable(
            initialCalendar[HOUR_OF_DAY],
            initialCalendar[MINUTE],
          )
          .await()
          ?: return@launch

        Calendar.getInstance()
          .apply {
            this.time = date
            this[HOUR_OF_DAY] = hourOfDay
            this[MINUTE] = minute
          }
          .time
          .let { if (isStart) viewModel.setStartDate(it) else viewModel.setEndDate(it) }
      }
    }
    binding.buttonStartTime.setOnClickListener(onClickListener)
    binding.buttonEndTime.setOnClickListener(onClickListener)
    binding.buttonClear.setOnClickListener(onClickListener)
  }

  private fun setupViews() {

  }
}