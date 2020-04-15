package com.doancnpm.edoctor.data.repository

import android.util.Base64
import arrow.core.Either
import arrow.core.Option
import arrow.core.extensions.fx
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.Mappers
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.repository.UserRepository
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class UserRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val userLocalSource: UserLocalSource,
  appCoroutineScope: CoroutineScope,
) : UserRepository {
  init {
    appCoroutineScope.launch {
      userLocalSource.token()
        ?: return@launch userLocalSource.removeUserAndToken()
      val user = userLocalSource.user()
        ?: return@launch userLocalSource.removeUserAndToken()

      try {
        val userResponse = apiService.getUserProfile(user.email)
        userLocalSource.saveUser(Mappers.userReponseToUserLocal(userResponse))
        Timber.d("[USER_REPO] init success")
      } catch (e: Exception) {
        Timber.d(e, "[USER_REPO] init failure: $e")

        if ((e as? HttpException)?.code() == 401) {
          userLocalSource.removeUserAndToken()
          Timber.d(e, "[USER_REPO] Login again!")
        }
      }
    }
  }

  override suspend fun login(email: String, password: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        val base64 = Base64.encodeToString(
          "$email:$password".toByteArray(),
          Base64.NO_WRAP,
        )
        val tokenResponse = apiService.login("Basic $base64")
        val token = tokenResponse.token ?: throw AppError.Remote.ServerError("Login failed", 401)
        userLocalSource.saveToken(token)

        val userResponse = apiService.getUserProfile(email)
        userLocalSource.saveUser(Mappers.userReponseToUserLocal(userResponse))
      }
    }
  }

  override fun userObservable(): Observable<Option<User>> {
    return Observables.combineLatest(
      userLocalSource.tokenObservable(),
      userLocalSource.userObservable(),
    ) { tokenOptional, userOptional ->
      Option.fx {
        val token = tokenOptional.bind()
        val user = userOptional.bind()
        User(
          name = user.name,
          email = user.email,
          createdAt = user.createdAt,
          imageUrl = user.imageUrl,
          token = token,
        )
      }
    }
  }
}