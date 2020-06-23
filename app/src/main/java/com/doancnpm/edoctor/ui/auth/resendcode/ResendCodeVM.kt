package com.doancnpm.edoctor.ui.auth.resendcode

import android.util.Patterns
import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeContract.*
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.exhaustMap
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom

class ResendCodeVM(
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
    val phoneObservable = intentS.ofType<ViewIntent.PhoneChanged>()
      .map { it.phone }
      .distinctUntilChanged()
      .map { getPhoneErrors(it) to it }
      .share()

    val resendChange = intentS.ofType<ViewIntent.Submit>()
      .withLatestFrom(phoneObservable) { _, phone -> phone }
      .filter { it.first.isEmpty() }
      .map { it.second }
      .exhaustMap { phone ->
        interactor
          .resendCode(phone)
          .doOnNext sendEvent@{ change ->
            singleEventD.value = Event(
              when (change) {
                is PartialStateChange.PhoneChanged -> return@sendEvent
                is PartialStateChange.PhoneErrors -> return@sendEvent
                PartialStateChange.Loading -> return@sendEvent
                is PartialStateChange.Success -> SingleEvent.Success(phone = phone)
                is PartialStateChange.Failure -> SingleEvent.Failure(
                  phone = phone,
                  error = change.error,
                )
                PartialStateChange.PhoneChangedFirstTime -> return@sendEvent
              }
            )
          }
      }

    Observable
      .mergeArray(
        resendChange,
        phoneObservable.map { PartialStateChange.PhoneChanged(phone = it.second) },
        phoneObservable.map { PartialStateChange.PhoneErrors(errors = it.first) },
        intentS.ofType<ViewIntent.PhoneChangedFirstTime>()
          .map { PartialStateChange.PhoneChangedFirstTime },
      )
      .observeOn(schedulers.main)
      .scan(initialViewState) { state, change -> change.reduce(state) }
      .subscribeBy(onNext = { viewStateD.value = it })
      .addTo(compositeDisposable)
  }

  private companion object {
    val invalidPhoneNumber = setOf(ValidationError.INVALID_PHONE_NUMBER)

    fun getPhoneErrors(phone: String): Set<ValidationError> {
      return if (Patterns.PHONE.matcher(phone).matches()) {
        emptySet()
      } else {
        invalidPhoneNumber
      }
    }
  }
}