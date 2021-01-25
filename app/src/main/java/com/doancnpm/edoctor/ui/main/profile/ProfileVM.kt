package com.doancnpm.edoctor.ui.main.profile

import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.entity.Option
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.main.profile.ProfileContract.SingleEvent
import com.doancnpm.edoctor.ui.main.profile.ProfileContract.ViewIntent
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.exhaustMap
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.rx3.rxSingle

class ProfileVM(
  private val userRepository: UserRepository,
  private val dispatchers: AppDispatchers,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val intentS = PublishRelay.create<ViewIntent>()
  private val eventD = MutableLiveData<Event<SingleEvent>>()
  private val isLoggingOutD = MutableLiveData<Boolean>().apply { value = false }

  val eventLiveData get() = eventD.asLiveData()
  val isLoggingOut get() = isLoggingOutD.asLiveData()
  val userObservable: Observable<Option<User>> = userRepository.userObservable()
    .map { it.getOrElse { Unit.left() } }
    .distinctUntilChanged()
    .observeOn(schedulers.main)
    .replay(1)
    .refCount()!!

  @CheckResult
  fun process(intents: Observable<ViewIntent>) = intents.subscribe(intentS)!!

  init {
    intentS
      .ofType<ViewIntent.Logout>()
      .exhaustMap {
        rxSingle(dispatchers.main) {
          isLoggingOutD.value = true

          userRepository
            .logout()
            .also { isLoggingOutD.value = false }
            .fold(
              ifLeft = { SingleEvent.LogoutFailure(it) },
              ifRight = { SingleEvent.LogoutSucess },
            )
        }.toObservable()
      }
      .subscribeBy { eventD.value = Event(it) }
      .addTo(compositeDisposable)
  }
}