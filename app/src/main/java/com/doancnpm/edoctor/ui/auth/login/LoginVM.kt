package com.doancnpm.edoctor.ui.auth.login

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.auth.login.LoginContract.*
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.exhaustMap
import com.doancnpm.edoctor.utils.mapNotNull
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom

class LoginVM(
  private val interactor: Interactor,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val stateD = MutableLiveData<ViewState>().apply { value = initialState }
  private val eventD = MutableLiveData<Event<SingleEvent>>()
  private val stateDistinctD = stateD.distinctUntilChanged()

  private val initialState = ViewState.initial()
  private val intentS = PublishRelay.create<ViewIntent>()

  val stateLiveData get() = stateDistinctD
  val eventLiveData get() = eventD.asLiveData()
  fun processIntents(intents: Observable<ViewIntent>): Disposable = intents.subscribe(intentS)!!

  init {
    val phoneObservable = intentS.ofType<ViewIntent.PhoneChanged>()
      .map { it.phone }
      .distinctUntilChanged()
      .map { it to getPhoneErrors(it) }
      .share()
    val passwordObservable = intentS.ofType<ViewIntent.PasswordChange>()
      .map { it.password }
      .distinctUntilChanged()
      .map { it to getPasswordErrors(it) }
      .share()

    val loginChanges = intentS
      .ofType<ViewIntent.SubmitLogin>()
      .withLatestFrom(phoneObservable, passwordObservable) { _, phone, password ->
        phone to password
      }
      .mapNotNull { (phonePair, passwordPair) ->
        val (phone, phoneErrors) = phonePair
        val (password, passwordErrors) = passwordPair

        if (phoneErrors.isEmpty() && passwordErrors.isEmpty()) {
          phone to password
        } else {
          null
        }
      }
      .exhaustMap { (phone, password) ->
        interactor
          .login(phone, password)
          .doOnNext {
            eventD.value = Event(
              when (it) {
                PartialChange.LoginSuccess -> SingleEvent.LoginSuccess
                is PartialChange.LoginFailure -> SingleEvent.LoginFailure(it.error)
                is PartialChange.PhoneError -> return@doOnNext
                is PartialChange.PasswordError -> return@doOnNext
                is PartialChange.PhoneChanged -> return@doOnNext
                is PartialChange.PasswordChanged -> return@doOnNext
                PartialChange.Loading -> return@doOnNext
              }
            )
          }
      }

    Observable.mergeArray(
      phoneObservable.map { PartialChange.PhoneError(it.second) },
      passwordObservable.map { PartialChange.PasswordError(it.second) },
      phoneObservable.map { PartialChange.PhoneChanged(it.first) },
      passwordObservable.map { PartialChange.PasswordChanged(it.first) },
      loginChanges,
    )
      .observeOn(schedulers.main)
      .scan(initialState) { state, change -> change.reduce(state) }
      .subscribeBy(onNext = { stateD.value = it })
      .addTo(compositeDisposable)
  }

  private companion object {
    // Singleton set of error
    val tooShortPassword = setOf(ValidationError.TOO_SHORT_PASSWORD)
    val invalidPhoneNumber = setOf(ValidationError.INVALID_PHONE_NUMBER)

    /**
     * @return error message or null if phone is valid
     */
    fun getPhoneErrors(phone: String): Set<ValidationError> {
      return when {
        phone.isBlank() -> invalidPhoneNumber
        !Patterns.PHONE.matcher(phone).matches() -> invalidPhoneNumber
        else -> emptySet()
      }
    }

    /**
     * @return error message or null if password is valid
     */
    fun getPasswordErrors(password: String): Set<ValidationError> {
      return when {
        password.length < 6 -> tooShortPassword
        else -> emptySet()
      }
    }
  }
}