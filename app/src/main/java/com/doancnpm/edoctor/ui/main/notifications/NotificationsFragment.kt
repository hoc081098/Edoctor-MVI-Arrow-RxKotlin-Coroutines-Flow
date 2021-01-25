package com.doancnpm.edoctor.ui.main.notifications

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentNotificationsBinding
import com.doancnpm.edoctor.domain.entity.Notification
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.MainActivity
import com.doancnpm.edoctor.ui.main.history.HistoryContract.HistoryType
import com.doancnpm.edoctor.ui.main.notifications.NotificationsContract.PlaceholderState
import com.doancnpm.edoctor.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
  private val binding: FragmentNotificationsBinding by viewBinding {
    recyclerView.removeOnScrollListener(onScrollListener)
    recyclerView.adapter = null
    retryButton.setOnClickListener(null)
    swipeRefreshLayout.setOnRefreshListener(null)
  }
  private val viewModel by viewModel<NotificationsVM>()

  private val serviceAdapter by lazy(LazyThreadSafetyMode.NONE) {
    NotificationsAdapter(
      GlideApp.with(this),
      ::onClickNotification,
    )
  }

  private val footerAdapter = FooterAdapter(::onRetry)
  private val onScrollListener by lazy(LazyThreadSafetyMode.NONE) {
    object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val linearLayoutManager = binding.recyclerView.layoutManager as LinearLayoutManager

        if (dy > 0 && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount) {
          viewModel.loadNextPage()
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.placeholderStateLiveData.observe(
      owner = viewLifecycleOwner,
      footerAdapter::submitList
    )
    viewModel.notificationsLiveData.observe(
      owner = viewLifecycleOwner,
      serviceAdapter::submitList,
    )
    viewModel.firstPagePlaceholderStateLiveData.observe(
      owner = viewLifecycleOwner,
      ::renderPlaceholderState,
    )
    viewModel.refreshingLiveData.observe(owner = viewLifecycleOwner) {
      if (it) {
        binding.swipeRefreshLayout.post { binding.swipeRefreshLayout.isRefreshing = true }
      } else {
        binding.swipeRefreshLayout.isRefreshing = false
      }
    }

    binding.recyclerView.addOnScrollListener(onScrollListener)
    binding.retryButton.setOnClickListener { viewModel.retryNextPage() }
    binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
  }

  private fun renderPlaceholderState(state: PlaceholderState) {
    binding.run {
      when (state) {
        PlaceholderState.Idle, is PlaceholderState.Success -> {
          errorGroup.gone()
          progressBar.invisible()

          if (state is PlaceholderState.Success) {
            if (state.isEmpty) textEmpty.visible()
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
          errorMessageTextView.text = state.error.getMessage()
          progressBar.invisible()
          textEmpty.invisible()
        }
      }
    }
  }

  private fun setupViews() {
    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = ConcatAdapter(serviceAdapter, footerAdapter)
    }
  }

  private fun onRetry() = viewModel.retryNextPage()

  private fun onClickNotification(notification: Notification) {
    (requireActivity() as MainActivity).navigateToHistory(
      type = when (val type = notification.type) {
        "Accepted mission" -> HistoryType.UP_COMING
        "Check QR Code" -> HistoryType.PROCESSING
        "Mission completed" -> HistoryType.DONE
        else -> HistoryType.WAITING.also {
          Timber.d("Invalid notification type: $type")
        }
      },
      orderId = notification.orderId ?: return,
    )
  }

  private companion object {
    const val VISIBLE_THRESHOLD = 1
  }
}