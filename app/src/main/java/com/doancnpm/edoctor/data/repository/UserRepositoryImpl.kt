package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import arrow.core.Option
import arrow.core.extensions.fx
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.Mappers
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.LoginUserBody
import com.doancnpm.edoctor.data.remote.body.RegisterUserBody
import com.doancnpm.edoctor.data.remote.response.BaseResponse
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.entity.rightResult
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.utils.catchError
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
  private val checkAuthDeferred = CompletableDeferred<Unit>()

  init {
    appCoroutineScope.launch { checkAuthInternal() }
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

  override suspend fun login(phone: String, password: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        val (token, user) = apiService.loginUser(
          LoginUserBody(
            phone = phone,
            password = password,
            deviceToken = "1234a" // TODO: Get device token
          )
        ).let(::unwrapResponse)

        userLocalSource.saveToken(token)
        userLocalSource.saveUser(Mappers.loginUserResponseToUserLocal(user))
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
              roleId = Mappers.roleIdToInt(roleId),
              fullName = fullName,
              birthday = birthday?.toString(),
            )
          )
          .let(::unwrapResponse)
          .phone
      }
    }
  }

  override fun userObservable(): Observable<DomainResult<Option<User>>> {
    return Observables.combineLatest(
      userLocalSource.tokenObservable(),
      userLocalSource.userObservable(),
    ) { tokenOptional, userOptional ->
      Option.fx {
        !tokenOptional
        val user = !userOptional
        Mappers.userLocalToUserDomain(user)
      }.rightResult()
    }.catchError(errorMapper)
  }

  /*
   * Private helpers
   */

  private suspend fun checkAuthInternal() {
    try {
      Timber.d("[USER_REPO] started")

      userLocalSource.token()
        ?: return userLocalSource.removeUserAndToken()
      userLocalSource.user()
        ?: return userLocalSource.removeUserAndToken()

      apiService.getCategories(page = 1, perPage = 1).let(::unwrapResponse)

      Timber.d("[USER_REPO] init success")
    } catch (e: Exception) {
      Timber.d(e, "[USER_REPO] init failure: $e")

      if ((e as? HttpException)?.code() in arrayOf(HTTP_UNAUTHORIZED, HTTP_FORBIDDEN)) {
        userLocalSource.removeUserAndToken()
        Timber.d(e, "[USER_REPO] Login again!")
      }
    } finally {
      checkAuthDeferred.complete(Unit)
    }
  }

  private companion object {
    /**
     * Return data in [baseResponse] if [baseResponse.success] is true.
     * Otherwise, throws [AppError.Remote.ServerError]
     * @param  baseResponse that needed to unwrap
     * @return Data in response
     * @throws AppError.Remote.ServerError
     */
    private fun <Data : Any> unwrapResponse(baseResponse: BaseResponse<Data>): Data {
      return if (baseResponse.success) {
        baseResponse.data
      } else {
        throw AppError.Remote.ServerError(
          errorMessage = "Response is not success",
          statusCode = -1
        )
      }
    }
  }
}