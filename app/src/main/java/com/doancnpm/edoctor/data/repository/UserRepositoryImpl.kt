package com.doancnpm.edoctor.data.repository

import android.util.Base64
import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.Mappers
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.repository.UserRepository
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val userLocalSource: UserLocalSource,
) : UserRepository {
  override suspend fun login(email: String, password: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        val base64 = Base64.encodeToString(
          "$email:$password".toByteArray(),
          Base64.NO_WRAP,
        )
        val tokenResponse = apiService.login("Basic $base64")
        val token = tokenResponse.token ?: throw AppError.Remote.ServerError("Login failed", 403)
        userLocalSource.saveToken(token)

        val userResponse = apiService.getUserProfile(email)
        userLocalSource.saveUser(Mappers.userReponseToUserLocal(userResponse))
      }
    }
  }
}