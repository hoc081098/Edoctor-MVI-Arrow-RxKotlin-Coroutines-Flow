package com.doancnpm.edoctor.ui.main.history

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHistoryBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.history.HistoryContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.recyclerview.scrollEvents
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.LazyThreadSafetyMode.NONE

class HistoryFragment : BaseFragment(R.layout.fragment_history) {
  private val binding by viewBinding<FragmentHistoryBinding>() {
    recyclerView.adapter = null
  }
  private val viewModel by viewModel<HistoryVM>()

  private val orderAdapter by lazy(NONE) { OrderAdapter(GlideApp.with(this)) }
  private val footerAdapter by lazy(NONE) { FooterAdapter(::onRetryNextPage) }

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

    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = MergeAdapter(orderAdapter, footerAdapter)
    }
  }

  private fun render(viewState: ViewState) = binding.run {
    Timber.d("State: $viewState")

    orderAdapter.submitList(viewState.orders)
    footerAdapter.submitList(viewState.nextPageState)

    when (val firstPageState = viewState.firstPageState) {
      PlaceholderState.Idle, is PlaceholderState.Success -> {
        errorGroup.gone()
        progressBar.invisible()

        if (firstPageState is PlaceholderState.Success) {
          if (firstPageState.isEmpty) textEmpty.visible()
          else textEmpty.invisible()
        }
      }
      PlaceholderState.Loading -> {
        errorGroup.gone()
        progressBar.visible()
        textEmpty.invisible()
      }
      is PlaceholderState.Error -> {
        errorGroup.visible()
        errorMessageTextView.text = firstPageState.error.getMessage()
        progressBar.invisible()
        textEmpty.invisible()
      }
    }
  }

  private fun intents(): Observable<ViewIntent> {
    val linearLayoutManager = binding.recyclerView.layoutManager as LinearLayoutManager

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
        .map(ViewIntent::ChangeType),
      binding.recyclerView
        .scrollEvents()
        .throttleLatest(200, TimeUnit.MILLISECONDS, true)
        .filter { (_, _, dy) ->
          dy > 0 && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount
        }
        .map { ViewIntent.LoadNextPage }
    )
  }

  private fun onRetryNextPage(): Unit = TODO()

  private companion object {
    const val VISIBLE_THRESHOLD = 1
  }
}