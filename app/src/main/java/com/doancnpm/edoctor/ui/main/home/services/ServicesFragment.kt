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
import com.doancnpm.edoctor.utils.observe
import com.doancnpm.edoctor.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.LazyThreadSafetyMode.NONE

class ServicesFragment : BaseFragment(R.layout.fragment_services) {
  private val binding by viewBinding { FragmentServicesBinding.bind(it) }
  private val navArgs by navArgs<ServicesFragmentArgs>()
  private val viewModel by viewModel<ServicesVM> { parametersOf(navArgs.category) }

  private val serviceAdapter by lazy(NONE) {
    ServiceAdapter(
      GlideApp.with(this),
    )
  }
  private val footerAdapter = FooterAdapter(::onRetry)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.loadingStateLiveData.observe(
      owner = viewLifecycleOwner,
      observer = footerAdapter::submitList
    )
    viewModel.userLiveData.observe(
      owner = viewLifecycleOwner,
      serviceAdapter::submitList,
    )

    binding.recyclerView.run {
      val linearLayoutManager = layoutManager as LinearLayoutManager

      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          if (dy > 0 && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount) {
            viewModel.loadNextPage()
          }
        }
      })
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
    const val VISIBLE_THRESHOLD = 2
  }
}