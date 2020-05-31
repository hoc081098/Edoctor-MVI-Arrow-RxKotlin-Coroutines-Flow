package com.doancnpm.edoctor.ui.main.home.services

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentServicesBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

class ServicesFragment : BaseFragment(R.layout.fragment_services) {
  private val binding: FragmentServicesBinding by viewBinding(
    {
      recyclerView.removeOnScrollListener(onScrollListener)
      recyclerView.adapter = null
      retryButton.setOnClickListener(null)
    }
  ) { FragmentServicesBinding.bind(it) }
  private val navArgs by navArgs<ServicesFragmentArgs>()
  private val viewModel by viewModel<ServicesVM> { parametersOf(navArgs.category) }

  private val serviceAdapter by lazy(NONE) {
    ServiceAdapter(
      GlideApp.with(this),
    )
  }
  private val footerAdapter = FooterAdapter(::onRetry)
  private val onScrollListener by lazy(NONE) {
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
      { footerAdapter.submitList(it); Timber.d("[LOADING_STATE_2] $it") }
    )
    viewModel.servicesLiveData.observe(
      owner = viewLifecycleOwner,
      serviceAdapter::submitList,
    )
    viewModel.firstPagePlaceholderStateLiveData.observe(
      owner = viewLifecycleOwner,
      ::renderPlaceholderState,
    )
    binding.recyclerView.addOnScrollListener(onScrollListener)
    binding.retryButton.setOnClickListener { viewModel.retryNextPage() }
  }

  private fun renderPlaceholderState(state: ServicesContract.PlaceholderState) {
    Timber.d("[LOADING_STATE_1] $state")

    binding.run {
      when (state) {
        ServicesContract.PlaceholderState.Idle -> {
          errorGroup.gone()
          progressBar.invisible()
        }
        ServicesContract.PlaceholderState.Loading -> {
          errorGroup.gone()
          progressBar.visible()
        }
        is ServicesContract.PlaceholderState.Error -> {
          errorGroup.visible()
          errorMessageTextView.text = state.error.getMessage()
          progressBar.invisible()
        }
      }
    }
  }

  private fun setupViews() {
    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = MergeAdapter(serviceAdapter, footerAdapter)
    }
  }

  private fun onRetry() = viewModel.retryNextPage()

  private companion object {
    const val VISIBLE_THRESHOLD = 1
  }
}