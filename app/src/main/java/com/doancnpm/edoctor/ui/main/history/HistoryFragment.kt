package com.doancnpm.edoctor.ui.main.history

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHistoryBinding
import com.doancnpm.edoctor.ui.main.history.HistoryContract.HistoryType
import com.doancnpm.edoctor.ui.main.history.HistoryContract.SingleEvent
import com.doancnpm.edoctor.ui.main.history.HistoryContract.ViewIntent
import com.doancnpm.edoctor.ui.main.history.HistoryContract.ViewState
import com.doancnpm.edoctor.utils.checkedIds
import com.doancnpm.edoctor.utils.observe
import com.doancnpm.edoctor.utils.observeEvent
import com.doancnpm.edoctor.utils.viewBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HistoryFragment : BaseFragment(R.layout.fragment_history) {
  private val binding by viewBinding<FragmentHistoryBinding>()
  private val viewModel by viewModel<HistoryVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.viewState.observe(owner = viewLifecycleOwner, ::render)
    viewModel.singleEvent.observeEvent(owner = viewLifecycleOwner, ::handleEvent)
    viewModel.process(intents()).addTo(compositeDisposable)
  }

  private fun handleEvent(singleEvent: SingleEvent) {
    TODO("handleEvent")
  }

  private fun setupViews() {
    binding.chipGroup.check(
      when (viewModel.viewState.value!!.type) {
        HistoryType.WAITING -> R.id.chip_waiting
        HistoryType.UP_COMING -> R.id.chip_up_coming
        HistoryType.PROCESSING -> R.id.chip_processing
        HistoryType.DONE -> R.id.chip_done
      }
    )
  }

  private fun render(state: ViewState) {
    Timber.d("State: $state")
  }

  private fun intents(): Observable<ViewIntent> {
    return Observable.mergeArray(
      binding.chipGroup
        .checkedIds()
        .distinctUntilChanged()
        .map {
          when (it) {
            R.id.chip_waiting -> HistoryType.WAITING
            R.id.chip_up_coming -> HistoryType.UP_COMING
            R.id.chip_processing -> HistoryType.PROCESSING
            R.id.chip_done -> HistoryType.DONE
            else -> error("Invalid checked id: $it")
          }
        }
        .map(ViewIntent::ChangeType)
    )
  }
}