package com.doancnpm.edoctor.ui.main.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.doancnpm.edoctor.GlideRequests
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerNotificationBinding
import com.doancnpm.edoctor.domain.entity.Notification
import com.doancnpm.edoctor.utils.toString_yyyyMMdd_HHmmss

class NotificationsAdapter(
  private val glide: GlideRequests,
  private val onClickNotification: (Notification) -> Unit,
) :
  ListAdapter<Notification, NotificationsAdapter.VH>(object : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Notification, newItem: Notification) = oldItem == newItem
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return ItemRecyclerNotificationBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).let(::VH)
  }

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  inner class VH(private val binding: ItemRecyclerNotificationBinding) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    init {
      binding.root.setOnClickListener(this)
    }

    fun bind(item: Notification) {
      binding.run {
        glide
          .load(item.image)
          .placeholder(R.drawable.logo)
          .thumbnail(0.5f)
          .centerCrop()
          .transition(withCrossFade())
          .into(imageView)

        textTitle.text = item.title
        textBody.text = item.body
        chipType.text = item.type
        textCreatedAt.text = item.createdAt.toString_yyyyMMdd_HHmmss()
      }
    }

    override fun onClick(v: View) {
      val position = bindingAdapterPosition
      if (position == RecyclerView.NO_POSITION) return
      val item = getItem(position)
      onClickNotification(item)
    }
  }
}