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

class LoginVM(
  private val interactor: Interactor,
  private val schedulers: AppSchedulers,
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
      .map { it to getEmailErrors(it) }
      .share()
    val passwordObservable = intentS.ofType<ViewIntent.PasswordChange>()
      .map { it.password }
      .map { it to getPasswordErrors(it) }
      .share()

    val loginChanges = intentS
      .ofType<ViewIntent.SubmitLogin>()
      .withLatestFrom(emailObservable, passwordObservable) { _, email, password ->
        email to password
      }
      .mapNotNull { (emailPair, passwordPair) ->
        val (email, emailErrors) = emailPair
        val (password, passwordErrors) = passwordPair
        if (emailErrors.isEmpty() && passwordErrors.isEmpty()) {
          email to password
        } else {
          null
        }
      }
      .exhaustMap { (email, password) ->
        interactor
          .login(email, password)
          .observeOn(schedulers.main)
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

    Observable.mergeArray(
        emailObservable.map { PartialChange.EmailError(it.second) },
        passwordObservable.map { PartialChange.PasswordError(it.second) },
        emailObservable.map { PartialChange.EmailChanged(it.first) },
        passwordObservable.map { PartialChange.PasswordChanged(it.first) },
        loginChanges,
      )
      .observeOn(schedulers.main)
      .scan(initialState) { state, change -> change.reduce(state) }
      .subscribeBy(onNext = { stateD.value = it })
      .addTo(compositeDisposable)
  }

  private companion object {
    /**
     * @return error message or null if email is valid
     */
    fun getEmailErrors(email: String): Set<ValidationError> {
      return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        emptySet()
      } else {
        setOf(ValidationError.INVALID_EMAIL_ADDRESS)
      }
    }

    /**
     * @return error message or null if password is valid
     */
    fun getPasswordErrors(password: String): Set<ValidationError> {
      return if (password.length < 6) {
        setOf(ValidationError.TOO_SHORT_PASSWORD)
      } else {
        emptySet()
      }
    }
  }
}