package com.doancnpm.edoctor.ui.main.profile

import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
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
import timber.log.Timber

class ProfileVM(
  private val userRepository: UserRepository,
  private val dispatchers: AppDispatchers,
) : BaseVM() {
  private val intentS = PublishRelay.create<ViewIntent>()
  private val eventD = MutableLiveData<Event<SingleEvent>>()

  val eventLiveData get() = eventD.asLiveData()

  @CheckResult
  fun process(intents: Observable<ViewIntent>) = intents.subscribe(intentS)!!

  init {
    intentS
      .ofType<ViewIntent.Logout>()
      .exhaustMap {
        rxSingle(dispatchers.main) {
          userRepository
            .logout()
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