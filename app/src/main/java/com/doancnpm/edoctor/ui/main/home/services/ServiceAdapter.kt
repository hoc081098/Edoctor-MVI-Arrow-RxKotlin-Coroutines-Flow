package com.doancnpm.edoctor.ui.main.home.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doancnpm.edoctor.GlideRequests
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerServiceBinding
import com.doancnpm.edoctor.domain.entity.Service

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
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Service) {
      binding.run {
        glide
          .load(item.image)
          .placeholder(R.drawable.splash_background)
          .thumbnail(0.5f)
          .centerCrop()
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(binding.imageView)

        binding.textName.text = item.name
      }
    }
  }
}