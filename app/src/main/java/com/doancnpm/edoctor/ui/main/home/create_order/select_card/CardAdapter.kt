package com.doancnpm.edoctor.ui.main.home.create_order.select_card

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.databinding.ItemRecyclerCardBinding
import com.doancnpm.edoctor.domain.entity.Card

class CardAdapter(
  private val onSelectCard: (Card) -> Unit,
) : ListAdapter<Card, CardAdapter.VH>(object : DiffUtil.ItemCallback<Card>() {
  override fun areItemsTheSame(oldItem: Card, newItem: Card) = oldItem.id == newItem.id
  override fun areContentsTheSame(oldItem: Card, newItem: Card) = oldItem == newItem
}) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
    ItemRecyclerCardBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false,
    )
  )

  override fun onBindViewHolder(holder: VH, position: Int): Unit = holder.bind(getItem(position))

  inner class VH(private val binding: ItemRecyclerCardBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
      binding.root.setOnClickListener {
        bindingAdapterPosition
          .takeIf { it != RecyclerView.NO_POSITION }
          ?.let { onSelectCard(getItem(it)) }
      }
    }

    fun bind(item: Card) {
      binding.run {
        imageView.setImageResource(item.imageDrawableId)
        textHoldername.text = item.cardHolderName
        textLast4.text = "••••${item.last4}"
      }
    }
  }
}