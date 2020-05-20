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
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy

class LoginVM(
  private val interactor: Interactor,
  private val rxSchedulerProvider: AppSchedulers,
) : BaseVM() {
  private val stateD = MutableLiveData<ViewState>().apply { value = initialState }
  private val eventD = MutableLiveData<Event<SingleEvent>>()

  private val initialState = ViewState.initial()
  private val intentS = PublishRelay.create<ViewIntent>()

  val stateLiveData get() = stateD.distinctUntilChanged()
  val eventLiveData get() = eventD.asLiveData()
  fun processIntents(intents: Observable<ViewIntent>): Disposable = intents.subscribe(intentS)!!

  init {
    val emailObservable = intentS.ofType<ViewIntent.EmailChanged>()
      .map { it.email }
      .share()
    val passwordObservable = intentS.ofType<ViewIntent.PasswordChange>()
      .map { it.password }
      .share()

    val emailErrorChanges = emailObservable.map { PartialChange.EmailError(getEmailError(it)) }
    val passwordErrorChange =
      passwordObservable.map { PartialChange.PasswordError(getPasswordError(it)) }

    val submit = intentS
      .ofType<ViewIntent.SubmitLogin>()
      .withLatestFrom(emailObservable, passwordObservable) { _, email, password ->
        email to password
      }

    val loginChanges = submit
      .filter { (email, password) ->
        getEmailError(email) === null && getPasswordError(password) === null
      }
      .exhaustMap { (email, password) ->
        interactor
          .login(email, password)
          .observeOn(rxSchedulerProvider.main)
          .doOnNext {
            eventD.value = Event(
              when (it) {
                PartialChange.LoginSuccess -> SingleEvent.LoginSuccess
                is PartialChange.LoginFailure -> SingleEvent.LoginFailure(it.error)
                is PartialChange.EmailError -> return@doOnNext
                is PartialChange.PasswordError -> return@doOnNext
                is PartialChange.EmailChanged -> return@doOnNext
                is PartialChange.PasswordChanged -> return@doOnNext
                PartialChange.Loading -> return@doOnNext
              }
            )
          }
      }

    val emailChange = emailObservable.map { PartialChange.EmailChanged(it) }
    val passwordChange = passwordObservable.map { PartialChange.PasswordChanged(it) }

    Observable.mergeArray(
        emailErrorChanges,
        passwordErrorChange,
        loginChanges,
        emailChange,
        passwordChange
      )
      .scan(initialState) { state, change -> change.reducer(state) }
      .observeOn(rxSchedulerProvider.main)
      .subscribeBy(onNext = { stateD.value = it })
      .addTo(compositeDisposable)
  }

  /**
   * @return error message or null if email is valid
   */
  private fun getEmailError(email: String): String? {
    return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      null
    } else {
      "Invalid email address"
    }
  }

  /**
   * @return error message or null if password is valid
   */
  private fun getPasswordError(password: String): String? {
    return if (password.length < 6) {
      "Min length of password is 6"
    } else {
      null
    }
  }
}