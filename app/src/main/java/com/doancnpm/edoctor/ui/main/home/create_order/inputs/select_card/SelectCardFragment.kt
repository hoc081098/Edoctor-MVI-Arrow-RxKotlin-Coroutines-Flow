package com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentSelectCardBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.SelectCardContract.CardItem
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalCoroutinesApi
class SelectCardFragment : BaseFragment(R.layout.fragment_select_card) {
  private val binding by viewBinding<FragmentSelectCardBinding>() {
    recyclerView.adapter = null
  }
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }
  private val cardAdapter by lazy(NONE) { CardAdapter(::onSelectCard) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.selectCardViewState.observe(owner = viewLifecycleOwner) { state ->
      cardAdapter.submitList(state.items)

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

            if (state.items.isEmpty()) {
              textEmpty.visible()
            } else {
              textEmpty.invisible()
            }
          }
        }
      }
    }

    binding.retryButton.setOnClickListener { viewModel.retryGetCards() }
  }

  private fun setupViews() {
    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = cardAdapter
    }

    binding.buttonAddCard.setOnClickListener { onClickAddCard() }
  }

  private fun onClickAddCard() = findNavController().navigate(R.id.action_global_addCardFragment)

  private fun onSelectCard(item: CardItem) = viewModel.select(item)
}