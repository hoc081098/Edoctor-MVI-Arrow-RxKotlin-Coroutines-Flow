package com.doancnpm.edoctor.ui.main.home.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.databinding.ItemRecyclerErrorLoadingBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.services.ServicesContract.LoadingState
import com.doancnpm.edoctor.utils.invisible
import com.doancnpm.edoctor.utils.visible

class FooterAdapter(private val onRetry: () -> Unit) :
  ListAdapter<LoadingState, FooterAdapter.VH>(object : DiffUtil.ItemCallback<LoadingState>() {
    override fun areItemsTheSame(oldItem: LoadingState, newItem: LoadingState) = true
    override fun areContentsTheSame(oldItem: LoadingState, newItem: LoadingState) =
      oldItem == newItem
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
    ItemRecyclerErrorLoadingBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  )

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  inner class VH(private val binding: ItemRecyclerErrorLoadingBinding) :
    RecyclerView.ViewHolder(binding.root) {
    init {
      binding.buttonRetry.setOnClickListener {
        onRetry()
      }
    }

    fun bind(item: LoadingState) {
      when (item) {
        LoadingState.Loading -> {
          binding.run {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.visible()
          }
        }
        is LoadingState.Error -> {
          binding.run {
            buttonRetry.visible()
            textError.visible()
            textError.text = item.error.getMessage()
            progressBar.invisible()
          }
        }
      }
    }
  }
}