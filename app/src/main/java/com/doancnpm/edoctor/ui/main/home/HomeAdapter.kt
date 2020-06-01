package com.doancnpm.edoctor.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.doancnpm.edoctor.GlideRequests
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.ItemRecyclerCategoryBinding
import com.doancnpm.edoctor.databinding.ItemRecyclerErrorLoadingBinding
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.HomeContract.Item
import com.doancnpm.edoctor.ui.main.home.HomeContract.PlaceholderState
import com.doancnpm.edoctor.utils.exhaustive
import com.doancnpm.edoctor.utils.invisible
import com.doancnpm.edoctor.utils.visible
import timber.log.Timber

@ExperimentalStdlibApi
class HomeAdapter(
  private val glide: GlideRequests,
  private val onClickRetry: () -> Unit,
  private val onClickCategory: (Category) -> Unit,
) :
  ListAdapter<Item, HomeAdapter.VH<ViewBinding>>(object : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
      return when {
        oldItem is Item.CategoryItem && newItem is Item.CategoryItem -> oldItem.category.id == newItem.category.id
        oldItem is Item.Placeholder && newItem is Item.Placeholder -> true
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem

    override fun getChangePayload(oldItem: Item, newItem: Item): Any? {
      return when {
        oldItem is Item.Placeholder && newItem is Item.Placeholder -> newItem.state
        else -> null
      }
    }
  }) {


  override fun onCreateViewHolder(parent: ViewGroup, @ViewType viewType: Int): VH<ViewBinding> {
    return when (viewType) {
      CATEGORY_VIEW_TYPE -> CategoryVH(
        ItemRecyclerCategoryBinding.inflate(
          LayoutInflater.from(parent.context),
          parent,
          false
        )
      )
      PLACEHOLDER_VIEW_TYPE -> PlaceholderVH(
        ItemRecyclerErrorLoadingBinding.inflate(
          LayoutInflater.from(parent.context),
          parent,
          false
        )
      )
      else -> error("Invalid viewType: $viewType")
    }
  }

  @ViewType
  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is Item.CategoryItem -> CATEGORY_VIEW_TYPE
      is Item.Placeholder -> PLACEHOLDER_VIEW_TYPE
    }
  }

  override fun onBindViewHolder(
    holder: VH<ViewBinding>,
    position: Int,
    payloads: MutableList<Any>,
  ) {
    if (payloads.isEmpty()) {
      return holder.bind(getItem(position))
    }

    payloads.forEach { payload ->
      if (holder is PlaceholderVH && payload is PlaceholderState) {
        holder.update(payload)
      }
    }
  }

  override fun onBindViewHolder(holder: VH<ViewBinding>, position: Int) =
    holder.bind(getItem(position))

  abstract class VH<out T : ViewBinding>(binding: T) : ViewHolder(binding.root) {
    abstract fun bind(item: Item)
  }

  inner class CategoryVH(private val binding: ItemRecyclerCategoryBinding) :
    VH<ItemRecyclerCategoryBinding>(binding) {
    init {
      itemView.setOnClickListener {
        val position = bindingAdapterPosition

        if (position == NO_POSITION) {
          return@setOnClickListener
        }

        val item = getItem(position)
        if (item is Item.CategoryItem) {
          onClickCategory(item.category)
        }
      }
    }

    override fun bind(item: Item) {
      val category = (item as? Item.CategoryItem ?: return).category

      binding.textName.text = category.name
      glide
        .load(category.image)
        .placeholder(R.drawable.splash_background)
        .thumbnail(0.5f)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(binding.imageView)
    }
  }

  inner class PlaceholderVH(private val binding: ItemRecyclerErrorLoadingBinding) :
    VH<ItemRecyclerErrorLoadingBinding>(binding) {
    init {
      binding.buttonRetry.setOnClickListener {
        if (bindingAdapterPosition != NO_POSITION) {
          onClickRetry()
        }
      }
    }

    override fun bind(item: Item) {
      if (item is Item.Placeholder) {
        update(item.state)
      }
    }

    fun update(state: PlaceholderState) {
      Timber.tag("[HOME]").d("Placeholder state: ${state::class.java.simpleName}")

      binding.run {
        when (state) {
          PlaceholderState.Loading -> {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.visible()
          }
          is PlaceholderState.Error -> {
            buttonRetry.visible()
            textError.visible()
            textError.text = state.error.getMessage()
            progressBar.invisible()
          }
          PlaceholderState.Idle -> {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.invisible()
          }
        }.exhaustive
      }
    }
  }

  companion object {
    const val CATEGORY_VIEW_TYPE = 0
    const val PLACEHOLDER_VIEW_TYPE = 1

    @IntDef(value = [CATEGORY_VIEW_TYPE, PLACEHOLDER_VIEW_TYPE])
    @Retention(AnnotationRetention.SOURCE)
    annotation class ViewType
  }
}