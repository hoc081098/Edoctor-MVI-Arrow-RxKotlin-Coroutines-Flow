package com.doancnpm.edoctor.ui.main.history

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
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
import com.doancnpm.edoctor.utils.*
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

  private val findDoctorS = PublishRelay.create<ViewIntent.FindDoctor>()
  val findDoctor get() = findDoctorS.asObservable()

  private val clickQRCodeS = PublishRelay.create<Order>()
  val clickQRCode get() = clickQRCodeS.asObservable()

  @LayoutRes
  override fun getItemViewType(position: Int) = layoutIdFor(getItem(position).status)

  override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): VH {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      R.layout.item_recycler_order_waiting -> WaitingVH(
        ItemRecyclerOrderWaitingBinding.inflate(inflater, parent, false),
        parent
      )
      R.layout.item_recycler_order_upcoming -> UpComingVH(
        ItemRecyclerOrderUpcomingBinding.inflate(inflater, parent, false),
        parent
      )
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

      binding.findDoctorButton
        .clicks()
        .takeUntil(parent.detaches())
        .mapNotNull {
          val position = bindingAdapterPosition
          if (position == RecyclerView.NO_POSITION) {
            null
          } else {
            ViewIntent.FindDoctor(getItem(position))
          }
        }
        .subscribe(findDoctorS)
        .addTo(compositeDisposable)
    }

    override fun bind(item: Order) {
      binding.run {
        glide
          .load(item.service!!.image)
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

  inner class UpComingVH(
    private val binding: ItemRecyclerOrderUpcomingBinding,
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

      binding.cardQrCode.clicks()
        .takeUntil(parent.detaches())
        .mapNotNull {
          val position = bindingAdapterPosition
          if (position == RecyclerView.NO_POSITION) null
          else getItem(position)
        }
        .subscribe(clickQRCodeS)
        .addTo(compositeDisposable)
    }

    override fun bind(item: Order) {
      binding.run {
        glide
          .load(item.service!!.image)
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

        item.doctor!!.avatar
          ?.let {
            glide
              .load(it)
              .placeholder(R.drawable.icons8_person_96)
              .error(R.drawable.icons8_person_96)
              .transition(DrawableTransitionOptions.withCrossFade())
              .into(imageAvatar)
          }
          ?: when (val firstLetter = item.doctor.fullName.firstOrNull()) {
            null -> ColorDrawable(Color.parseColor("#fafafa"))
            else -> {
              val size = binding.root.context
                .dpToPx(64)
              TextDrawable
                .builder()
                .beginConfig()
                .width(size)
                .height(size)
                .endConfig()
                .buildRect(
                  firstLetter.toUpperCase().toString(),
                  ColorGenerator.MATERIAL.getColor(item.doctor.phone),
                )
            }
          }.let(imageAvatar::setImageDrawable)

        textDoctorName.text = item.doctor.fullName
      }
    }
  }

  inner class ProcessingVH(private val binding: ItemRecyclerOrderProcessingBinding) : VH(binding.root) {
    override fun bind(item: Order) {
      binding.run {
        glide
          .load(item.service!!.image)
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

        //language=HTML
        textView.text =  HtmlCompat.fromHtml(
          "Doctor <b>${item.doctor!!.fullName}</b> is doing...",
          FROM_HTML_MODE_LEGACY
        )
      }
    }
  }

  class DoneVH(private val binding: ItemRecyclerOrderDoneBinding) : VH(binding.root) {
    override fun bind(item: Order) {
      TODO("Not yet implemented")
    }
  }
  //endregion
}