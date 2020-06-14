package com.doancnpm.edoctor.ui.main.home.create_order.select_card

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentSelectCardBinding
import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalCoroutinesApi
class SelectCardFragment : BaseFragment(R.layout.fragment_select_card) {
  private val binding by viewBinding<FragmentSelectCardBinding>() {
    recyclerView.adapter = null
  }
  private val viewModel by viewModel<SelectCardVM>()
  private val cardAdapter by lazy(NONE) { CardAdapter(::onSelectCard) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.viewState.observe(owner = viewLifecycleOwner) { state ->
      cardAdapter.submitList(state.cards)

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

            if (state.cards.isEmpty()) {
              textEmpty.visible()
            } else {
              textEmpty.invisible()
            }
          }
        }
      }
    }

    binding.retryButton.setOnClickListener { viewModel.retry() }
  }

  private fun setupViews() {
    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = cardAdapter
    }

    binding.buttonAddCard.setOnClickListener { onClickAddCard() }
  }

  private fun onClickAddCard() {
    SelectCardFragmentDirections
      .actionSelectCardFragmentToAddCardFragment()
      .let { findNavController().navigate(it) }
  }

  private fun onSelectCard(card: Card) {
    context?.toast("Select $card")
  }
}