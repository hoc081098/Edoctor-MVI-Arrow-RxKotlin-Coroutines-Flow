package com.doancnpm.edoctor.ui.main.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.databinding.ItemRecyclerErrorLoadingBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.notifications.NotificationsContract.PlaceholderState
import com.doancnpm.edoctor.utils.invisible
import com.doancnpm.edoctor.utils.visible

class FooterAdapter(private val onRetry: () -> Unit) :
  ListAdapter<PlaceholderState, FooterAdapter.VH>(object :
    DiffUtil.ItemCallback<PlaceholderState>() {
    override fun areItemsTheSame(oldItem: PlaceholderState, newItem: PlaceholderState) = true
    override fun areContentsTheSame(oldItem: PlaceholderState, newItem: PlaceholderState) =
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

    fun bind(item: PlaceholderState) {
      when (item) {
        PlaceholderState.Loading -> {
          binding.run {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.visible()
          }
        }
        is PlaceholderState.Error -> {
          binding.run {
            buttonRetry.visible()
            textError.visible()
            textError.text = item.error.getMessage()
            progressBar.invisible()
          }
        }
        is PlaceholderState.Idle -> {
          binding.run {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.invisible()
          }
        }
      }
    }
  }
}