package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import arrow.core.Option
import arrow.core.extensions.fx
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.LoginUserBody
import com.doancnpm.edoctor.data.remote.body.RegisterUserBody
import com.doancnpm.edoctor.data.toInt
import com.doancnpm.edoctor.data.toUserDomain
import com.doancnpm.edoctor.data.toUserLocal
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.entity.rightResult
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.utils.catchError
import com.doancnpm.edoctor.utils.toString_yyyyMMdd
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.*

class UserRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val userLocalSource: UserLocalSource,
  appCoroutineScope: CoroutineScope,
) : UserRepository {
  private val userObservable: Observable<Either<AppError, Option<User>>> =
    Observables
      .combineLatest(
        userLocalSource.tokenObservable(),
        userLocalSource.userObservable(),
      ) { tokenOptional, userOptional ->
        Option.fx {
          !tokenOptional
          val user = !userOptional
          user.toUserDomain()
        }.rightResult()
      }
      .catchError(errorMapper)
      .distinctUntilChanged()
      .replay(1)
      .refCount()

  private val checkAuthDeferred = CompletableDeferred<Unit>()

  init {
    appCoroutineScope.launch {
      while (isActive) {
        checkAuthInternal()
        delay(CHECK_AUTH_INTERVAL)
      }
    }
  }

  /*
   * Implement UserRepository
   */

  override suspend fun checkAuth(): DomainResult<Boolean> {
    return Either.catch(errorMapper::map) {
      checkAuthDeferred.await()
      userLocalSource.token() !== null && userLocalSource.user() !== null
    }
  }

  override suspend fun logout(): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      userLocalSource.removeUserAndToken()
    }
  }

  override suspend fun resendCode(phone: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        apiService.resendCode(phone).unwrap()
      }
    }
  }

  override suspend fun verify(phone: String, code: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        apiService.verifyUser(
          phone = phone,
          code = code
        ).unwrap()
      }
    }
  }

  override suspend fun login(phone: String, password: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        val (token, user) = apiService.loginUser(
          LoginUserBody(
            phone = phone,
            password = password,
            deviceToken = "1234a" // TODO: Get device token
          )
        ).unwrap()

        userLocalSource.saveToken(token)
        userLocalSource.saveUser(user.toUserLocal())
      }
    }
  }

  override suspend fun register(
    phone: String,
    password: String,
    roleId: User.RoleId,
    fullName: String,
    birthday: Date?,
  ): DomainResult<String> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        apiService
          .registerUser(
            RegisterUserBody(
              phone = phone,
              password = password,
              roleId = roleId.toInt(),
              fullName = fullName,
              birthday = birthday?.toString_yyyyMMdd(),
            )
          )
          .unwrap()
          .phone
      }
    }
  }

  override fun userObservable() = userObservable

  /*
   * Private helpers
   */

  private suspend fun checkAuthInternal() {
    try {
      Timber.d("[CHECK AUTH] started")

      userLocalSource.token()
        ?: return userLocalSource.removeUserAndToken()
      userLocalSource.user()
        ?: return userLocalSource.removeUserAndToken()

      apiService.getCategories(page = 1, perPage = 1).unwrap()

      Timber.d("[CHECK AUTH] success")
    } catch (e: Exception) {
      Timber.d(e, "[CHECK AUTH] failure: $e")

      if ((e as? HttpException)?.code() in arrayOf(HTTP_UNAUTHORIZED, HTTP_FORBIDDEN)) {
        userLocalSource.removeUserAndToken()
        Timber.d(e, "[CHECK AUTH] Login again!")
      }
    } finally {
      checkAuthDeferred.complete(Unit)
    }
  }

  private companion object {
    const val CHECK_AUTH_INTERVAL = 60_000L
  }
}