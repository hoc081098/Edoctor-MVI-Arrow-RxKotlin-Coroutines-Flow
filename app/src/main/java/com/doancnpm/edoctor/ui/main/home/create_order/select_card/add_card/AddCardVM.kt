package com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card.AddCardContract.*
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
      .map { it.cvc }
      .distinctUntilChanged()
      .map { getCvcErrors(it) to it }

    val errorsAndFormData = Observables.combineLatest(
      nameObservable,
      numberObservable,
      dateObservable,
      cvcObservable,
    ) { name, number, date, cvc ->
      val errors = name.first + number.first + date.first + cvc.first
      val (expiredMonth, expiredYear) = expiredDateRegex.findAll(date.second)
        .map { it.value.toInt() }.toList()
      val formData = FormData.Data(
        holderName = name.second,
        number = number.second,
        expiredMonth = expiredMonth,
        expiredYear = expiredYear,
        cvc = cvc.second,
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
          .addCard(data)
          .doOnNext sendEvent@{ change ->
            eventD.value = Event(
              when (change) {
                is PartialChange.ErrorsAndFormDataChanged -> return@sendEvent
                PartialChange.Loading -> return@sendEvent
                is PartialChange.Success -> SingleEvent.Success
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
    val emptyHolderName = setOf(ValidationError.EMPTY_HOLDER_NAME)
    val invalidNumber = setOf(ValidationError.INVALID_NUMBER)
    val expiredDateRegex = "^(1[0-2]|[1-9])/(\\d{2})$".toRegex()
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
      val component = expiredDateRegex.findAll(date).map { it.value.toInt() }.toList()
      return when {
        component.size != 2 -> invalidExpiredDate
        else -> {
          val (month, year) = component

          val calendar = Calendar.getInstance()
          val currentMonth = calendar[Calendar.MONTH]
          val currentYear = calendar[Calendar.YEAR]

          when {
            year < currentYear -> invalidExpiredDate
            year == currentYear -> if (month < currentMonth) invalidExpiredDate else emptySet()
            else -> emptySet()
          }
        }
      }
    }

    fun getCvcErrors(cvc: Int): Set<ValidationError> {
      return if (cvc.toString().length != 3) {
        invalidCvc
      } else {
        emptySet()
      }
    }
  }
}