package com.doancnpm.edoctor.ui.main.home.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doancnpm.edoctor.GlideRequests
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerServiceBinding
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.utils.toast

class ServiceAdapter(
  private val glide: GlideRequests,
) :
  ListAdapter<Service, ServiceAdapter.VH>(object : DiffUtil.ItemCallback<Service>() {
    override fun areItemsTheSame(oldItem: Service, newItem: Service) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Service, newItem: Service) = oldItem == newItem
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return ItemRecyclerServiceBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    ).let(::VH)
  }

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  inner class VH(private val binding: ItemRecyclerServiceBinding) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    init {
      binding.detailButton.setOnClickListener(this)
    }

    fun bind(item: Service) {
      binding.run {
        glide
          .load(item.image)
          .placeholder(R.drawable.splash_background)
          .thumbnail(0.5f)
          .centerCrop()
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(imageView)

        textName.text = item.name
        textPrice.text = item.price.toString()
        textDescription.text = item.description
      }
    }

    override fun onClick(v: View) {
      val position = bindingAdapterPosition
      if (position == RecyclerView.NO_POSITION) return

      val item = getItem(position)

      when (v) {
        binding.detailButton -> v.context.toast("Clicked $item")
        else -> TODO()
      }
    }
  }
}