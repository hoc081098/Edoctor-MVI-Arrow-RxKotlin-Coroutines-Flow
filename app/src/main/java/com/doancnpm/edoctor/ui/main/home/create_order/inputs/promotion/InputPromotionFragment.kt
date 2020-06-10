package com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputPromotionBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionContract.PromotionItem
import com.doancnpm.edoctor.utils.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.LazyThreadSafetyMode.NONE

class InputPromotionFragment : BaseFragment(R.layout.fragment_input_promotion) {
  private val binding by viewBinding<FragmentInputPromotionBinding>() {
    recyclerView.adapter = null
  }
  private val parentViewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }
  private val viewModel by viewModel<InputPromotionVM>()

  private val promotionAdapter by lazy(NONE) { PromotionAdapter(::onSelectItem) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun setupViews() {
    binding.run {
      recyclerView.run {
        setHasFixedSize(true)
        layoutManager = GridLayoutManager(context, 2)
        adapter = promotionAdapter
      }
    }
  }

  private fun bindVM() {
    viewModel.viewState.observe(owner = viewLifecycleOwner) { state ->
      promotionAdapter.submitList(state.promotions)

      binding.run {
        if (state.isLoading) {
          progressBar.visible()
          errorGroup.gone()
        } else {
          progressBar.invisible()

          if (state.error !== null) {
            errorGroup.visible()
            errorMessageTextView.text = state.error.getMessage()
          } else {
            errorGroup.gone()
          }
        }
      }
    }
    viewModel.selectedItem.observe(viewLifecycleOwner, Observer {
      parentViewModel.setPromotion(it?.promotion)
    })

    binding.retryButton.setOnClickListener { viewModel.retry() }
  }

  private fun onSelectItem(item: PromotionItem) = viewModel.select(item)
}