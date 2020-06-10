package com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerPromotionBinding
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionContract.PromotionItem
import com.doancnpm.edoctor.utils.toString_yyyyMMdd_HHmmss
import kotlin.math.roundToInt

class PromotionAdapter(
  private val onSelect: (PromotionItem) -> Unit,
) :
  ListAdapter<PromotionItem, PromotionAdapter.VH>(object : DiffUtil.ItemCallback<PromotionItem>() {
    override fun areItemsTheSame(oldItem: PromotionItem, newItem: PromotionItem) =
      oldItem.promotion.id == newItem.promotion.id

    override fun areContentsTheSame(oldItem: PromotionItem, newItem: PromotionItem) =
      oldItem == newItem
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return VH(
      ItemRecyclerPromotionBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: VH, position: Int): Unit = holder.bind(
    getItem(position),
    gradients[position % gradients.size]
  )

  inner class VH(private val binding: ItemRecyclerPromotionBinding) :
    RecyclerView.ViewHolder(binding.root) {

    init {
      itemView.setOnClickListener listener@{
        val position = bindingAdapterPosition
        if (position == RecyclerView.NO_POSITION) return@listener

        val item = getItem(position)
        onSelect(item)
      }
    }

    fun bind(item: PromotionItem, @DrawableRes backgroundId: Int) {
      binding.run {
        backgroundView.setBackgroundResource(backgroundId)

        val promotion = item.promotion
        textName.text = promotion.name

        val discount = (promotion.discount * 100).roundToInt()
        textDiscount.text = "$discount% OFF"

        textStartDate.text = "From: ${promotion.startDate.toString_yyyyMMdd_HHmmss()}"
        textEndDate.text = "To: ${promotion.endDate.toString_yyyyMMdd_HHmmss()}"

        if (item.isSelected) {
          imageChecked.setImageResource(R.drawable.ic_baseline_check_circle_24)
        } else {
          imageChecked.setImageDrawable(null)
        }
      }
    }
  }

  private companion object {
    val gradients = listOf(
      R.drawable.gradient1,
      R.drawable.gradient2,
      R.drawable.gradient3,
      R.drawable.gradient4,
      R.drawable.gradient5,
      R.drawable.gradient6,
    )
  }
}
