package com.doancnpm.edoctor.ui.auth.register

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.auth.register.RegisterContract.*
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.exhaustMap
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.*

class RegisterVM(
  private val interactor: Interactor,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val stateD = MutableLiveData<ViewState>().apply { value = initialState }
  private val eventD = MutableLiveData<Event<SingleEvent>>()

  private val initialState = ViewState.initial()
  private val intentS = PublishRelay.create<ViewIntent>()

  val stateLiveData get() = stateD.distinctUntilChanged()
  val eventLiveData get() = eventD.asLiveData()
  fun processIntents(intents: Observable<ViewIntent>): Disposable = intents.subscribe(intentS)!!

  init {
    val phoneObservable = intentS.ofType<ViewIntent.PhoneChanged>()
      .map { it.phone }
      .distinctUntilChanged()
      .map { getPhoneErrors(it) to it }

    val passwordObservable = intentS.ofType<ViewIntent.PasswordChanged>()
      .map { it.password }
      .distinctUntilChanged()
      .map { getPasswordErrors(it) to it }

    val roleIdObservable = intentS.ofType<ViewIntent.RoleIdChanged>()
      .map { it.roleId }
      .distinctUntilChanged()
      .map { emptySet<ValidationError>() to it }

    val fullNameObservable = intentS.ofType<ViewIntent.FullNameChanged>()
      .map { it.fullName }
      .distinctUntilChanged()
      .map { getFullNameErrors(it) to it }

    val birthDayObservable = intentS.ofType<ViewIntent.BirthdayChanged>()
      .map { it.date }
      .distinctUntilChanged()
      .map { getBirthDayErrors(it) to it }

    val errorsAndFormData = Observables.combineLatest(
        phoneObservable,
        passwordObservable,
        roleIdObservable,
        fullNameObservable,
        birthDayObservable
      ) { phone, password, roleId, fullName, birthDay ->
        val errors = phone.first + password.first + roleId.first + fullName.first + birthDay.first
        val formData = FormData.Data(
          phone = phone.second,
          password = password.second,
          roleId = roleId.second,
          fullName = fullName.second,
          birthday = birthDay.second,
        )
        errors to formData
      }
      .distinctUntilChanged()
      .share()

    val registerChange = intentS.ofType<ViewIntent.Submit>()
      .withLatestFrom(errorsAndFormData) { _, pair -> pair }
      .filter { (errors) -> errors.isEmpty() }
      .map { (_, formData) -> formData }
      .exhaustMap { data ->
        interactor
          .register(data)
          .doOnNext sendEvent@{ change ->
            eventD.value = Event(
              when (change) {
                is PartialChange.ErrorsAndFormDataChanged -> return@sendEvent
                PartialChange.Loading -> return@sendEvent
                is PartialChange.Success -> SingleEvent.Success(change.phone)
                is PartialChange.Failure -> SingleEvent.Failure(change.error)
              }
            )
          }
      }

    Observable.mergeArray(
        errorsAndFormData.map {
          PartialChange.ErrorsAndFormDataChanged(
            errors = it.first,
            formData = it.second,
          )
        },
        registerChange,
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
    val tooShortFullName = setOf(ValidationError.TOO_SHORT_FULL_NAME)

    /**
     * @return phone errors
     */
    fun getPhoneErrors(phone: String): Set<ValidationError> {
      return when {
        phone.isBlank() -> invalidPhoneNumber
        !Patterns.PHONE.matcher(phone).matches() -> invalidPhoneNumber
        else -> emptySet()
      }
    }

    /**
     * @return password errors
     */
    fun getPasswordErrors(password: String): Set<ValidationError> {
      return when {
        password.length < 6 -> tooShortPassword
        else -> emptySet()
      }
    }

    /**
     * @return full name errors
     */
    fun getFullNameErrors(fullName: String): Set<ValidationError> {
      return when {
        fullName.length < 6 -> tooShortFullName
        else -> emptySet()
      }
    }

    fun getBirthDayErrors(birthDay: Date): Set<ValidationError> {
      return emptySet() // TODO: getBirthDayErrors
    }
  }
}