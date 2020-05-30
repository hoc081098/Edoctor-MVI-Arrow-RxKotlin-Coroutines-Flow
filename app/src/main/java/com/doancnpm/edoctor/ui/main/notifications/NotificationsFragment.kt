package com.doancnpm.edoctor.ui.main.notifications

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentNotificationsBinding
import com.doancnpm.edoctor.utils.inflate
import com.doancnpm.edoctor.utils.viewBinding

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
  private val binding by viewBinding { FragmentNotificationsBinding.bind(it) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = object : ListAdapter<String, VH>(object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
          return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
          return oldItem == newItem
        }

      }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
          return VH(parent inflate android.R.layout.simple_list_item_2)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
          holder.itemView.findViewById<TextView>(android.R.id.text1).text = getItem(position)
          holder.itemView.findViewById<TextView>(android.R.id.text2).text = getItem(position)
        }

      }.apply { submitList((0..50).map { "Item $it" }) }
    }
  }

  class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

  }
}