package com.doancnpm.edoctor.ui.auth.verify

import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.auth.verify.VerifyContract.*
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.exhaustMap
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom

class VerifyVM(
  private val phone: String,
  private val interactor: Interactor,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val intentS = PublishRelay.create<ViewIntent>()

  private val singleEventD = MutableLiveData<Event<SingleEvent>>()

  private val initialViewState = ViewState()
  private val viewStateD = MutableLiveData<ViewState>().apply { value = initialViewState }
  private val viewStateDistinctD = viewStateD.distinctUntilChanged()

  val viewState get() = viewStateDistinctD
  val singleEvent get() = singleEventD

  @CheckResult
  fun process(intents: Observable<ViewIntent>) = intents.subscribe(intentS)!!

  init {
    val phoneObservable = intentS.ofType<ViewIntent.CodeChanged>()
      .map { it.code }
      .distinctUntilChanged()
      .map { getCodeErrors(it) to it }
      .share()

    val resendChange = intentS.ofType<ViewIntent.Submit>()
      .withLatestFrom(phoneObservable) { _, code -> code }
      .filter { it.first.isEmpty() }
      .map { it.second }
      .exhaustMap { code ->
        interactor
          .verify(phone = phone, code = code)
          .doOnNext sendEvent@{ change ->
            singleEventD.value = Event(
              when (change) {
                is PartialStateChange.CodeChanged -> return@sendEvent
                is PartialStateChange.CodeErrors -> return@sendEvent
                PartialStateChange.Loading -> return@sendEvent
                is PartialStateChange.Success -> SingleEvent.Success
                is PartialStateChange.Failure -> SingleEvent.Failure(change.error, )
              }
            )
          }
      }

    Observable
      .mergeArray(
        resendChange,
        phoneObservable.map { PartialStateChange.CodeChanged(code = it.second) },
        phoneObservable.map { PartialStateChange.CodeErrors(errors = it.first) },
      )
      .observeOn(schedulers.main)
      .scan(initialViewState) { state, change -> change.reduce(state) }
      .subscribeBy(onNext = { viewStateD.value = it })
      .addTo(compositeDisposable)
  }

  private companion object {
    val emptyCode = setOf(ValidationError.EMPTY_CODE)

    fun getCodeErrors(code: String): Set<ValidationError> {
      return if (code.isBlank()) {
        emptyCode
      } else {
        emptySet()
      }
    }
  }
}