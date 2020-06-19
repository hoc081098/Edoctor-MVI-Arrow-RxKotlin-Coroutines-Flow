package com.doancnpm.edoctor.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doancnpm.edoctor.databinding.ItemRecyclerErrorLoadingBinding
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.history.HistoryContract.PlaceholderState
import com.doancnpm.edoctor.ui.main.history.HistoryContract.ViewIntent
import com.doancnpm.edoctor.utils.asObservable
import com.doancnpm.edoctor.utils.invisible
import com.doancnpm.edoctor.utils.visible
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit

class FooterAdapter(
  private val schedulers: AppSchedulers,
  private val compositeDisposable: CompositeDisposable,
) :
  ListAdapter<PlaceholderState, FooterAdapter.VH>(object :
    DiffUtil.ItemCallback<PlaceholderState>() {
    override fun areItemsTheSame(oldItem: PlaceholderState, newItem: PlaceholderState) = true
    override fun areContentsTheSame(oldItem: PlaceholderState, newItem: PlaceholderState) =
      oldItem == newItem
  }) {

  private val retryNextPageS = PublishRelay.create<ViewIntent.RetryNextPage>()
  val retryNextPage get() = retryNextPageS.asObservable()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
    ItemRecyclerErrorLoadingBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  )

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  inner class VH(private val binding: ItemRecyclerErrorLoadingBinding) :
    RecyclerView.ViewHolder(binding.root) {
    init {
      binding.buttonRetry.clicks()
        .throttleFirst(200, TimeUnit.MILLISECONDS, schedulers.main)
        .map { ViewIntent.RetryNextPage }
        .subscribe(retryNextPageS)
        .addTo(compositeDisposable)
    }

    fun bind(item: PlaceholderState) {
      when (item) {
        PlaceholderState.Loading -> {
          binding.run {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.visible()
          }
        }
        is PlaceholderState.Error -> {
          binding.run {
            buttonRetry.visible()
            textError.visible()
            textError.text = item.error.getMessage()
            progressBar.invisible()
          }
        }
        PlaceholderState.Idle, is PlaceholderState.Success -> {
          binding.run {
            buttonRetry.invisible()
            textError.invisible()
            progressBar.invisible()
          }
        }
      }
    }
  }
}