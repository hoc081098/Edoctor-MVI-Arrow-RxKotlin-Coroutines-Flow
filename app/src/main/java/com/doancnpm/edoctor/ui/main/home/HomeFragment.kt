package com.doancnpm.edoctor.ui.main.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentHomeBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.HomeContract.ViewState
import com.doancnpm.edoctor.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalStdlibApi
class HomeFragment : BaseFragment(R.layout.fragment_home) {
  private val binding by viewBinding(
    {
      recyclerView.adapter = null
      swipeRefreshLayout.setOnRefreshListener(null)
      retryButton.setOnClickListener(null)
    }
  ) { FragmentHomeBinding.bind(it) }

  private val viewModel by viewModel<HomeVM>()
  private val homeAdapter by lazy(NONE) {
    HomeAdapter(
      GlideApp.with(this),
      ::onRetryClick,
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.stateLiveData.observe(owner = viewLifecycleOwner, observer = ::render)

    binding.recyclerView.run {
      val layoutManager = layoutManager as GridLayoutManager

      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          if (dy > 0 && layoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= layoutManager.itemCount) {
            viewModel.loadNextPage()
          }
        }
      })
    }

    binding.swipeRefreshLayout.setOnRefreshListener {
      viewModel.refresh()
    }

    binding.retryButton.setOnClickListener {
      viewModel.retryNextPage()
    }
  }

  private fun setupViews() {
    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = GridLayoutManager(
        context,
        if (context.isOrientationPortrait) 2 else 4,
        RecyclerView.VERTICAL,
        false
      ).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
          override fun getSpanSize(position: Int): Int {
            return when (homeAdapter.getItemViewType(position)) {
              HomeAdapter.CATEGORY_VIEW_TYPE -> 1
              HomeAdapter.PLACEHOLDER_VIEW_TYPE -> 2
              else -> error("Invalid viewType")
            }
          }
        }
      }
      adapter = homeAdapter
    }
  }

  private fun onRetryClick() = viewModel.retryNextPage()

  private fun render(state: ViewState) {
    homeAdapter.submitList(state.items)

    binding.run {
      Timber.d("Home state: ${state.isLoadingFirstPage}..${state.firstPageError}")

      when {
        state.isLoadingFirstPage -> {
          progressBar.visible()
          errorGroup.gone()
        }
        else -> {
          progressBar.invisible()

          if (state.firstPageError !== null) {
            errorMessageTextView.text = state.firstPageError.getMessage()
            errorGroup.visible()
          } else {
            errorGroup.gone()
          }
        }
      }
    }
  }

  private companion object {
    const val VISIBLE_THRESHOLD = 2
  }
}