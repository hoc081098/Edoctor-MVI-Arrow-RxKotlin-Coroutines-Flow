package com.doancnpm.edoctor.ui.main.history

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ConcatAdapter
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHistoryBinding
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.MainVM
import com.doancnpm.edoctor.ui.main.history.HistoryContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.recyclerview.scrollEvents
import com.jakewharton.rxbinding4.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.LazyThreadSafetyMode.NONE

class HistoryFragment : BaseFragment(R.layout.fragment_history) {
  private val binding by viewBinding<FragmentHistoryBinding>() {
    recyclerView.adapter = null
  }
  private val viewModel by viewModel<HistoryVM>()
  private val schedulers by inject<AppSchedulers>()
  private val mainVM by sharedViewModel<MainVM>()

  private val orderAdapter by lazy(NONE) { OrderAdapter(GlideApp.with(this), compositeDisposable) }
  private val footerAdapter by lazy(NONE) {
    FooterAdapter(
      schedulers = schedulers,
      compositeDisposable = compositeDisposable
    )
  }

  private val intentsS = PublishRelay.create<ViewIntent>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val viewState = viewModel.viewState.value!!

    val initialHistoryType = mainVM.getHistoryType() ?: viewState.type
    val orderId = mainVM.getOrderId()

    setupViews(initialHistoryType)
    bindVM(initialHistoryType, orderId)

    // dispatch retry or refresh intent when needed
    if (viewState.canRetryFirstPage) {
      intentsS.accept(ViewIntent.RetryFirstPage)
    }
  }

  private fun bindVM(initialHistoryType: HistoryType, orderId: Long?) {
    viewModel.viewState.observe(owner = viewLifecycleOwner, ::render)
    viewModel.singleEvent.observeEvent(owner = viewLifecycleOwner, ::handleEvent)
    viewModel
      .process(
        intents(
          initialHistoryType,
          orderId
        )
      )
      .addTo(compositeDisposable)
  }

  private fun handleEvent(singleEvent: SingleEvent) {
    when (singleEvent) {
      is SingleEvent.Cancel.Success -> {
        view?.snack("Cancel successfully")
      }
      is SingleEvent.Cancel.Failure -> {
        view?.snack("Cancel failure: ${singleEvent.error.getMessage()}")
      }
      is SingleEvent.FindDoctor.Success -> {
        view?.snack("Push notifications to doctors successfully")
      }
      is SingleEvent.FindDoctor.Failure -> {
        view?.snack("Push notifications to doctors failure: ${singleEvent.error.getMessage()}")
      }
    }.exhaustive
  }

  private fun setupViews(initialHistoryType: HistoryType) {
    val position = HistoryType.values().indexOf(initialHistoryType)

    position
      .let(binding.tabLayout::getTabAt)
      .let(binding.tabLayout::selectTab)

    binding.tabLayout.doOnPreDraw {
      binding.tabLayout.setScrollPosition(
        position,
        0f,
        true
      )
    }

    Timber.d("[###] [0] $initialHistoryType")

    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = ConcatAdapter(orderAdapter, footerAdapter)
    }

    orderAdapter.clickQRCode
      .subscribeBy { order ->
        QRCodeFragment
          .newInstance(order.id)
          .let { requireActivity().showAlertDialog(it) }
      }
      .addTo(compositeDisposable)
  }

  private fun render(viewState: ViewState) = binding.run {
    Timber.d("State: ${viewState.description}")

    orderAdapter.submitList(viewState.orders) {
      viewModel.scrollToTop
        ?.getContentIfNotHandled()
        ?.let { recyclerView.scrollToPosition(0) }
    }
    footerAdapter.submitList(viewState.nextPageState)

    if (viewState.isRefreshing) {
      binding.swipeRefreshLayout.post { binding.swipeRefreshLayout.isRefreshing = true }
    } else {
      binding.swipeRefreshLayout.isRefreshing = false
    }

    when (val firstPageState = viewState.firstPageState) {
      PlaceholderState.Idle, is PlaceholderState.Success -> {
        errorGroup.gone()
        progressBar.invisible()

        if (firstPageState is PlaceholderState.Success) {
          if (firstPageState.isEmpty) emptyLayout.visible()
          else emptyLayout.invisible()
        }
      }
      PlaceholderState.Loading -> {
        errorGroup.gone()
        progressBar.visible()
        emptyLayout.invisible()
      }
      is PlaceholderState.Error -> {
        errorGroup.visible()
        errorMessageTextView.text = firstPageState.error.getMessage()
        progressBar.invisible()
        emptyLayout.invisible()
      }
    }
  }

  private fun intents(initialHistoryType: HistoryType, orderId: Long?): Observable<ViewIntent> {
    val linearLayoutManager = binding.recyclerView.layoutManager as LinearLayoutManager

    return Observable.mergeArray(
      intentsS,
      binding.tabLayout
        .selectedTabPositions()
        .map { HistoryType.values().getOrElse(it) { HistoryType.WAITING } }
        .doOnNext { Timber.d("[###] [1] $it") }
        .map {
          ViewIntent.ChangeType(
            historyType = it,
            orderId = null,
          )
        }
        .startWithItem(
          ViewIntent.ChangeType(
            historyType = initialHistoryType,
            orderId = orderId,
          )
        ),
      binding.recyclerView
        .scrollEvents()
        .throttleLatest(200, TimeUnit.MILLISECONDS, schedulers.main, true)
        .filter { (_, _, dy) ->
          dy > 0 && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount
        }
        .map { ViewIntent.LoadNextPage },
      orderAdapter
        .cancelOrder
        .exhaustMap { intent ->
          requireActivity()
            .showAlertDialogAsObservable {
              title("Cancel")
              message("This action cannot be undone")
              cancelable(true)
              iconId(R.drawable.ic_baseline_cancel_24)
            }
            .map { intent }
        },
      orderAdapter
        .findDoctor
        .exhaustMap { intent ->
          requireActivity()
            .showAlertDialogAsObservable {
              title("Find doctor")
              message("Find doctor for this order")
              cancelable(true)
              iconId(R.drawable.ic_baseline_find_in_page_24)
            }
            .map { intent }
        },
      binding.retryButton
        .clicks()
        .throttleFirst(200, TimeUnit.MILLISECONDS, schedulers.main)
        .map { ViewIntent.RetryFirstPage },
      footerAdapter.retryNextPage,
      binding.swipeRefreshLayout
        .refreshes()
        .map { ViewIntent.Refresh },
    )
  }

  private companion object {
    const val VISIBLE_THRESHOLD = 1
  }
}