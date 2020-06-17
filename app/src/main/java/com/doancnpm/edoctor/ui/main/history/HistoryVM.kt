package com.doancnpm.edoctor.ui.main.history

import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.main.history.HistoryContract.Interactor
import com.doancnpm.edoctor.ui.main.history.HistoryContract.SingleEvent
import com.doancnpm.edoctor.ui.main.history.HistoryContract.ViewIntent
import com.doancnpm.edoctor.ui.main.history.HistoryContract.ViewState
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.setValue
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy

class HistoryVM(
  private val interactor: Interactor,
  private val schedulers: AppSchedulers,
) : BaseVM() {
  private val intentS = PublishRelay.create<ViewIntent>()
  private val stateD = MutableLiveData<ViewState>()
  private val singleEventD = MutableLiveData<Event<SingleEvent>>()

  val viewState get() = stateD.asLiveData()
  val singleEvent get() = singleEventD.asLiveData()
  @CheckResult
  fun process(intents: Observable<ViewIntent>) = intents.subscribe(intentS)!!

  init {
    val initialState = ViewState()
    stateD.value = initialState

    val changeTypeChange = intentS
      .ofType<ViewIntent.ChangeType>()
      .map { it.historyType }
      .distinctUntilChanged()
      .switchMap { type ->
        interactor.load(
          page = 1,
          perPage = PER_PAGE,
          serviceName = null,
          date = null,
          orderId = null,
          type = type
        )
      }


    Observable
      .mergeArray(
        changeTypeChange
      )
      .observeOn(schedulers.main)
      .scan(initialState) { vs, change -> change.reduce(vs) }
      .subscribeBy { state -> stateD.setValue { state } }
      .addTo(compositeDisposable)
  }

  private companion object {
    const val PER_PAGE = 4
  }
}