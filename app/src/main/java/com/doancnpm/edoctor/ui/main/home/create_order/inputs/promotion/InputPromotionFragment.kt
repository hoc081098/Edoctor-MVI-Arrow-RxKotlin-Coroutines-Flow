package com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputPromotionBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.InputPromotionContract.PromotionItem
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalCoroutinesApi
class InputPromotionFragment : BaseFragment(R.layout.fragment_input_promotion) {
  private val binding by viewBinding<FragmentInputPromotionBinding>() {
    recyclerView.adapter = null
  }
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

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
    viewModel.promotionViewState.observe(owner = viewLifecycleOwner) { state ->
      promotionAdapter.submitList(state.promotions)

      binding.run {
        if (state.isLoading) {
          progressBar.visible()
          errorGroup.gone()
          textEmpty.invisible()
        } else {
          progressBar.invisible()

          if (state.error !== null) {
            errorGroup.visible()
            errorMessageTextView.text = state.error.getMessage()
            textEmpty.invisible()
          } else {
            errorGroup.gone()

            if (state.promotions.isEmpty()) {
              textEmpty.visible()
            } else {
              textEmpty.invisible()
            }
          }
        }
      }
    }
    binding.retryButton.setOnClickListener { viewModel.retryGetPromotion() }
  }

  private fun onSelectItem(item: PromotionItem) = viewModel.select(item)
}