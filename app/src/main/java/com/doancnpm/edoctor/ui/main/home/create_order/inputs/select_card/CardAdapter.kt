package com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerCardBinding
import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderContract.SelectCardContract.CardItem

class CardAdapter(
  private val onSelectCard: (CardItem) -> Unit,
) : ListAdapter<CardItem, CardAdapter.VH>(object : DiffUtil.ItemCallback<CardItem>() {
  override fun areItemsTheSame(oldItem: CardItem, newItem: CardItem) = oldItem.card.id == newItem.card.id
  override fun areContentsTheSame(oldItem: CardItem, newItem: CardItem) = oldItem == newItem
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

    fun bind(item: CardItem) {
      val card = item.card
      binding.run {
        imageView.setImageResource(card.imageDrawableId)
        textHoldername.text = card.cardHolderName
        textLast4.text = "••••${card.last4}"

        if (item.isSelected) {
          imageChecked.setImageResource(R.drawable.ic_baseline_check_circle_24)
        } else {
          imageChecked.setImageDrawable(null)
        }
      }
    }
  }
}