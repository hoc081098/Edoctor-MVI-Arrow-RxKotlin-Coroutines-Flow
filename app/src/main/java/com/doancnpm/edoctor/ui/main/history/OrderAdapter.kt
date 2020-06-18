package com.doancnpm.edoctor.ui.main.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doancnpm.edoctor.GlideRequests
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerOrderDoneBinding
import com.doancnpm.edoctor.databinding.ItemRecyclerOrderProcessingBinding
import com.doancnpm.edoctor.databinding.ItemRecyclerOrderUpcomingBinding
import com.doancnpm.edoctor.databinding.ItemRecyclerOrderWaitingBinding
import com.doancnpm.edoctor.domain.entity.Order
import com.doancnpm.edoctor.ui.main.history.HistoryContract.HistoryType.Helper.layoutIdFor
import com.doancnpm.edoctor.ui.main.history.HistoryContract.ViewIntent
import com.doancnpm.edoctor.utils.asObservable
import com.doancnpm.edoctor.utils.currencyVndFormatted
import com.doancnpm.edoctor.utils.mapNotNull
import com.doancnpm.edoctor.utils.toString_yyyyMMdd_HHmmss
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.detaches
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

class OrderAdapter(
  private val glide: GlideRequests,
  private val compositeDisposable: CompositeDisposable,
) : ListAdapter<Order, OrderAdapter.VH>(object : DiffUtil.ItemCallback<Order>() {
  override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
  override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
}) {
  private val cancelOrderS = PublishRelay.create<ViewIntent.Cancel>()
  val cancelOrder get() = cancelOrderS.asObservable()

  @LayoutRes
  override fun getItemViewType(position: Int) = layoutIdFor(getItem(position).status)

  override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): VH {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      R.layout.item_recycler_order_waiting -> WaitingVH(
        ItemRecyclerOrderWaitingBinding.inflate(inflater, parent, false),
        parent
      )
      R.layout.item_recycler_order_upcoming -> UpComingVH(ItemRecyclerOrderUpcomingBinding.inflate(inflater, parent, false))
      R.layout.item_recycler_order_processing -> ProcessingVH(ItemRecyclerOrderProcessingBinding.inflate(inflater, parent, false))
      R.layout.item_recycler_order_done -> DoneVH(ItemRecyclerOrderDoneBinding.inflate(inflater, parent, false))
      else -> error("Invalid viewType: $viewType")
    }
  }

  //region ViewHolders
  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  abstract class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: Order)
  }

  inner class WaitingVH(
    private val binding: ItemRecyclerOrderWaitingBinding,
    parent: ViewGroup
  ) : VH(binding.root) {
    init {
      binding.cancelButton
        .clicks()
        .takeUntil(parent.detaches())
        .mapNotNull {
          val position = bindingAdapterPosition
          if (position == RecyclerView.NO_POSITION) {
            null
          } else {
            ViewIntent.Cancel(getItem(position))
          }
        }
        .subscribe(cancelOrderS)
        .addTo(compositeDisposable)
    }

    override fun bind(item: Order) {
      binding.run {
        glide
          .load(item.service.image)
          .placeholder(R.drawable.logo)
          .thumbnail(0.5f)
          .centerCrop()
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(imageView)
        textName.text = item.service.name

        //language=HTML
        textStartTime.text = HtmlCompat.fromHtml(
          "<b>Start time: </b>${item.startTime.toString_yyyyMMdd_HHmmss()}",
          FROM_HTML_MODE_LEGACY
        )
        //language=HTML
        textEndTime.text = HtmlCompat.fromHtml(
          "<b>End time: </b>${item.endTime.toString_yyyyMMdd_HHmmss()}",
          FROM_HTML_MODE_LEGACY
        )
        //language=HTML
        textAddress.text = HtmlCompat.fromHtml(
          "<b>Address: </b>${item.address}",
          FROM_HTML_MODE_LEGACY
        )
        //language=HTML
        textNote.text = HtmlCompat.fromHtml(
          "<b>Note: </b>${item.note ?: ""}",
          FROM_HTML_MODE_LEGACY
        )

        textTotalPrice.text = "Total: ${item.total.currencyVndFormatted}"
      }
    }
  }

  class UpComingVH(private val binding: ItemRecyclerOrderUpcomingBinding) : VH(binding.root) {
    override fun bind(item: Order) {
      TODO("Not yet implemented")
    }
  }

  class ProcessingVH(private val binding: ItemRecyclerOrderProcessingBinding) : VH(binding.root) {
    override fun bind(item: Order) {
      TODO("Not yet implemented")
    }
  }

  class DoneVH(private val binding: ItemRecyclerOrderDoneBinding) : VH(binding.root) {
    override fun bind(item: Order) {
      TODO("Not yet implemented")
    }
  }
  //endregion
}