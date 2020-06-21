package com.doancnpm.edoctor.ui.main.profile.update_profile

import androidx.lifecycle.MutableLiveData
import arrow.core.toOption
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.main.profile.update_profile.UpdateProfileContract.PartialChange
import com.doancnpm.edoctor.ui.main.profile.update_profile.UpdateProfileContract.SingleEvent
import com.doancnpm.edoctor.ui.main.profile.update_profile.UpdateProfileContract.ViewIntent
import com.doancnpm.edoctor.ui.main.profile.update_profile.UpdateProfileContract.ViewState
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.kotlin.withLatestFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable

@ExperimentalCoroutinesApi
class UpdateProfileVM(
  user: User,
  private val userRepository: UserRepository,
  private val dispatchers: AppDispatchers,
  schedulers: AppSchedulers,
) : BaseVM() {
  private val stateD = MutableLiveData<ViewState>().apply { value = initialState }
  private val eventD = MutableLiveData<Event<SingleEvent>>()

  private val initialState = ViewState.initial(user)
  private val intentS = PublishRelay.create<ViewIntent>()

  val stateLiveData get() = stateD.asLiveData()
  val eventLiveData get() = eventD.asLiveData()
  fun processIntents(intents: Observable<ViewIntent>): Disposable = intents.subscribe(intentS)!!

  init {
    val fullNameObservable = intentS.ofType<ViewIntent.FullNameChanged>()
      .map { it.name.trim() }
      .distinctUntilChanged()
      .map { it to (it.length < 6) }
      .share()

    val birthdayChanged = intentS.ofType<ViewIntent.BirthdayChanged>()
      .map { it.date.toOption() }
      .distinctUntilChanged()
      .share()

    val avatarChanged = intentS.ofType<ViewIntent.AvatarChanged>()
      .map { it.avatar.toOption() }
      .distinctUntilChanged()
      .share()

    val submitChange = intentS.ofType<ViewIntent.Submit>()
      .withLatestFrom(
        o1 = fullNameObservable,
        o2 = birthdayChanged,
        o3 = avatarChanged
      ) { _, fullName, birthday, avatar ->
        Triple(fullName, birthday.orNull(), avatar.orNull())
      }
      .mapNotNull { (fullName, birthday, avatar) ->
        if (fullName.second) {
          null
        } else {
          Triple(fullName.first, birthday, avatar)
        }
      }
      .exhaustMap { (fullName, birthday, avatar) ->
        rxObservable(dispatchers.main) {
          send(PartialChange.Loading)

          userRepository
            .updateUserProfile(
              fullName = fullName,
              birthday = birthday,
              avatar = avatar,
            )
            .fold(
              ifLeft = { PartialChange.Failure(it) },
              ifRight = { PartialChange.Success },
            )
            .let { send(it) }
        }
      }
      .doOnNext { change ->
        eventD.value = Event(
          when (change) {
            is PartialChange.AvatarChanged -> return@doOnNext
            is PartialChange.FullNameChanged -> return@doOnNext
            is PartialChange.BirthdayChanged -> return@doOnNext
            PartialChange.Loading -> return@doOnNext
            PartialChange.Success -> SingleEvent.Success
            is PartialChange.Failure -> SingleEvent.Failure(change.error)
          }
        )
      }

    Observable
      .mergeArray(
        fullNameObservable.map {
          PartialChange.FullNameChanged(
            name = it.first,
            tooShortFullName = it.second
          )
        },
        birthdayChanged.map { PartialChange.BirthdayChanged(date = it.orNull()) },
        avatarChanged.map { PartialChange.AvatarChanged(avatar = it.orNull()) },
        submitChange,
      )
      .observeOn(schedulers.main)
      .scan(initialState) { vs, change -> change.reduce(vs) }
      .subscribe { state -> stateD.setValue { state } }
      .addTo(compositeDisposable)
  }
}