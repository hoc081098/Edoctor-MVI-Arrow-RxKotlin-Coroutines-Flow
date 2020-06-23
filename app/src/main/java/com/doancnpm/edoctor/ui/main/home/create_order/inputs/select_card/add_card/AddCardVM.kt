package com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card.add_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card.add_card.AddCardContract.*
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.exhaustMap
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.*
import java.util.Calendar

class AddCardVM(
  private val interactor: Interactor,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val stateD = MutableLiveData<ViewState>().apply { value = initialState }
  private val eventD = MutableLiveData<Event<SingleEvent>>()
  private val stateDistinctD = stateD.distinctUntilChanged()

  private val initialState = AddCardContract.ViewState.initial()
  private val intentS = PublishRelay.create<ViewIntent>()

  val stateLiveData get() = stateDistinctD
  val eventLiveData get() = eventD.asLiveData()
  fun processIntents(intents: Observable<ViewIntent>): Disposable = intents.subscribe(intentS)!!

  init {
    val nameObservable = intentS.ofType<ViewIntent.HolderNameChanged>()
      .map { it.name.trim() }
      .distinctUntilChanged()
      .map { getNameErrors(it) to it }

    val numberObservable = intentS.ofType<ViewIntent.NumberChanged>()
      .map { it.number.trim() }
      .distinctUntilChanged()
      .map { getNumberErrors(it) to it }

    val dateObservable = intentS.ofType<ViewIntent.ExpiredDateChanged>()
      .map { it.date.trim() }
      .distinctUntilChanged()
      .map { getExpiredDateErrors(it) to it }

    val cvcObservable = intentS.ofType<ViewIntent.CvcChanged>()
      .map { it.cvc.trim() }
      .distinctUntilChanged()
      .map { getCvcErrors(it) to it }

    val errorsAndFormData = Observables.combineLatest(
      nameObservable,
      numberObservable,
      dateObservable,
      cvcObservable,
    ) { name, number, date, cvc ->
      val errors = name.first + number.first + date.first + cvc.first

      val (expiredMonth, expiredYear) = expiredDateRegex.matchEntire(date.second)
        ?.destructured
        ?.let { (month, year) -> month.toIntOrNull() to year.toIntOrNull() }
        ?: null to null
      val cvcInt = cvc.second.toIntOrNull()

      errors to if (expiredMonth != null && expiredYear != null && cvcInt != null) {
        FormData.Data(
          holderName = name.second,
          number = number.second,
          expiredMonth = expiredMonth,
          expiredYear = expiredYear,
          cvc = cvcInt,
        )
      } else {
        FormData.Invalid(
          holderName = name.second,
          number = number.second,
          expiredMonth = expiredMonth,
          expiredYear = expiredYear,
          cvc = cvcInt,
        )
      }
    }
      .distinctUntilChanged()
      .share()

    val registerChange = intentS.ofType<ViewIntent.Submit>()
      .withLatestFrom(errorsAndFormData) { _, pair -> pair }
      .filter { (errors) -> errors.isEmpty() }
      .map { (_, formData) -> formData }
      .ofType<FormData.Data>()
      .exhaustMap { data ->
        interactor
          .addCard(data)
          .doOnNext sendEvent@{ change ->
            eventD.value = Event(
              when (change) {
                is PartialChange.ErrorsAndFormDataChanged -> return@sendEvent
                PartialChange.Loading -> return@sendEvent
                is PartialChange.Success -> SingleEvent.Success
                is PartialChange.Failure -> SingleEvent.Failure(change.error)
                PartialChange.HolderNameChangedFirstTime -> return@sendEvent
                PartialChange.NumberChangedFirstTime -> return@sendEvent
                PartialChange.ExpiredDateChangedFirstTime -> return@sendEvent
                PartialChange.CvcChangedFirstTime -> return@sendEvent
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
      intentS.ofType<ViewIntent.HolderNameChangedFirstTime>().map { PartialChange.HolderNameChangedFirstTime },
      intentS.ofType<ViewIntent.NumberChangedFirstTime>().map { PartialChange.NumberChangedFirstTime },
      intentS.ofType<ViewIntent.ExpiredDateChangedFirstTime>().map { PartialChange.ExpiredDateChangedFirstTime },
      intentS.ofType<ViewIntent.CvcChangedFirstTime>().map { PartialChange.CvcChangedFirstTime },
    )
      .observeOn(schedulers.main)
      .scan(initialState) { state, change -> change.reduce(state) }
      .subscribeBy(onNext = { stateD.value = it })
      .addTo(compositeDisposable)
  }

  private companion object {
    val emptyHolderName = setOf(ValidationError.EMPTY_HOLDER_NAME)
    val invalidNumber = setOf(ValidationError.INVALID_NUMBER)
    val expiredDateRegex = "^([1-9]|0[1-9]|1[0-2])/(\\d{2})$".toRegex()
    val invalidExpiredDate = setOf(ValidationError.INVALID_EXPIRED_DATE)
    val invalidCvc = setOf(ValidationError.INVALID_CVC)

    fun getNameErrors(name: String): Set<ValidationError> {
      return if (name.isBlank()) {
        emptyHolderName
      } else {
        emptySet()
      }
    }

    fun getNumberErrors(number: String): Set<ValidationError> {
      return if (number.length != 16) {
        invalidNumber
      } else {
        emptySet()
      }
    }

    fun getExpiredDateErrors(date: String): Set<ValidationError> {
      val groups = expiredDateRegex.matchEntire(date)?.groupValues ?: return invalidExpiredDate

      return when {
        groups.size != 3 -> invalidExpiredDate
        else -> {
          val (_, month, year) = groups.map { it.toIntOrNull() }
          if (month == null || year == null) {
            return invalidExpiredDate
          }

          val calendar = Calendar.getInstance()
          val currentMonth = calendar[Calendar.MONTH]
          val currentYear = calendar[Calendar.YEAR] % 100

          when {
            year < currentYear -> invalidExpiredDate
            year == currentYear -> if (month < currentMonth) invalidExpiredDate else emptySet()
            else -> emptySet()
          }
        }
      }
    }

    fun getCvcErrors(cvc: String): Set<ValidationError> {
      return if (cvc.length != 3 || cvc.toIntOrNull() === null) {
        invalidCvc
      } else {
        emptySet()
      }
    }
  }
}