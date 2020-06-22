package com.doancnpm.edoctor.ui.main.history

import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.main.history.HistoryContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom
import timber.log.Timber

class HistoryVM(
  private val interactor: Interactor,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val intentS = PublishRelay.create<ViewIntent>()
    .apply {
      subscribe { Timber.d(">>>>>>> History intent: $it") }
        .addTo(compositeDisposable)
    }
  private val stateD = MutableLiveData<ViewState>()
  private val singleEventD = MutableLiveData<Event<SingleEvent>>()

  val viewState get() = stateD.asLiveData()
  val singleEvent get() = singleEventD.asLiveData()
  var scrollToTop: Event<Unit>? = null
    private set

  @CheckResult
  fun process(intents: Observable<ViewIntent>) = intents.subscribe(intentS)!!

  init {
    val initialState = ViewState()
    val stateS = BehaviorRelay.createDefault(initialState)
    stateD.value = initialState

    val changeTypeIntent = intentS.ofType<ViewIntent.ChangeType>().share()

    val changeTypeChange = changeTypeIntent
      .distinctUntilChanged()
      .doOnNext { Timber.d("[###] [2] $it") }
      .switchMap { intent ->
        interactor.load(
          page = 1,
          perPage = PER_PAGE,
          serviceName = null,
          date = null,
          orderId = intent.orderId,
          type = intent.historyType,
        )
      }

    val nextPageChange = intentS
      .ofType<ViewIntent.LoadNextPage>()
      .withLatestFrom(stateS)
      .mapNotNull { (_, viewState) ->
        if (viewState.canLoadNextPage) viewState.page + 1 to viewState.type
        else null
      }
      .exhaustMap { (page, type) ->
        interactor
          .load(
            page = page,
            perPage = PER_PAGE,
            serviceName = null,
            date = null,
            orderId = null,
            type = type
          )
          .takeUntil(changeTypeIntent)
      }

    val cancelOrderChange = intentS.ofType<ViewIntent.Cancel>()
      .groupBy { it.order.id }
      .flatMap { intents -> intents.exhaustMap { interactor.cancel(it.order) } }
      .doOnNext { change ->
        when (change) {
          is PartialChange.Cancel.Success -> SingleEvent.Cancel.Success(change.order)
          is PartialChange.Cancel.Failure -> SingleEvent.Cancel.Failure(change.order, change.error)
        }.let { singleEventD.value = Event(it) }
      }

    val findDoctorChange = intentS.ofType<ViewIntent.FindDoctor>()
      .groupBy { it.order.id }
      .flatMap { intents -> intents.exhaustMap { interactor.findDoctor(it.order) } }
      .doOnNext { change ->
        when (change) {
          is PartialChange.FindDoctor.Success -> SingleEvent.FindDoctor.Success(change.order)
          is PartialChange.FindDoctor.Failure -> SingleEvent.FindDoctor.Failure(change.order, change.error)
        }.let { singleEventD.value = Event(it) }
      }

    val retryFirstPageChange = intentS.ofType<ViewIntent.RetryFirstPage>()
      .withLatestFrom(stateS)
      .mapNotNull { (_, viewState) ->
        if (viewState.canRetryFirstPage) viewState.type
        else null
      }
      .exhaustMap { type ->
        interactor
          .load(
            page = 1,
            perPage = PER_PAGE,
            serviceName = null,
            date = null,
            orderId = null,
            type = type,
          )
          .takeUntil(changeTypeIntent)
      }

    val retryNextPageChange = intentS.ofType<ViewIntent.RetryNextPage>()
      .withLatestFrom(stateS)
      .mapNotNull { (_, viewState) ->
        if (viewState.canRetryNextPage) viewState.page + 1 to viewState.type
        else null
      }
      .exhaustMap { (page, type) ->
        interactor
          .load(
            page = page,
            perPage = PER_PAGE,
            serviceName = null,
            date = null,
            orderId = null,
            type = type
          )
          .takeUntil(changeTypeIntent)
      }

    val refreshChange = intentS.ofType<ViewIntent.Refresh>()
      .withLatestFrom(stateS) { _, vs -> vs.type }
      .exhaustMap { type ->
        interactor
          .refresh(
            perPage = PER_PAGE,
            serviceName = null,
            date = null,
            orderId = null,
            type = type
          )
          .takeUntil(changeTypeIntent)
          .doOnNext { change ->
            scrollToTop = when (change) {
              PartialChange.Refresh.Refreshing -> null
              is PartialChange.Refresh.Success -> Event(Unit)
              is PartialChange.Refresh.Failure -> null
            }
          }
      }

    Observable
      .mergeArray(
        changeTypeChange,
        nextPageChange,
        cancelOrderChange,
        findDoctorChange,
        retryFirstPageChange,
        retryNextPageChange,
        refreshChange,
      )
      .observeOn(schedulers.main)
      .doOnNext { Timber.d(">>>>>>> Change: $it") }
      .scan(initialState) { vs, change -> change.reduce(vs) }
      .subscribe(stateS)
      .addTo(compositeDisposable)

    stateS.subscribeBy { state -> stateD.setValue { state } }.addTo(compositeDisposable)
  }

  private companion object {
    const val PER_PAGE = 6
  }
}