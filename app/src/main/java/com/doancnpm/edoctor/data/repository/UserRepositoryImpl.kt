package com.doancnpm.edoctor.data.repository

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import arrow.core.Either
import arrow.core.Option
import arrow.core.extensions.fx
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.LoginUserBody
import com.doancnpm.edoctor.data.remote.body.RegisterUserBody
import com.doancnpm.edoctor.data.remote.body.UpdateProfileBody
import com.doancnpm.edoctor.data.toInt
import com.doancnpm.edoctor.data.toUserDomain
import com.doancnpm.edoctor.data.toUserLocal
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.entity.rightResult
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.utils.UTCTimeZone
import com.doancnpm.edoctor.utils.catchError
import com.doancnpm.edoctor.utils.toString_yyyyMMdd
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.Date

class UserRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val userLocalSource: UserLocalSource,
  private val firebaseInstanceId: FirebaseInstanceId,
  private val baseUrl: String,
  private val application: Application,
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
      val deviceToken = firebaseInstanceId.instanceId.await().token
      apiService.logout(deviceToken).unwrap()
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

  override suspend fun updateUserProfile(fullName: String, birthday: Date?, avatar: Uri?): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      val user = apiService
        .updateProfile(
          UpdateProfileBody(
            fullName = fullName,
            birthday = birthday?.toString_yyyyMMdd(),
            avatar = uploadAvatar(avatar)
          )
        )
        .unwrap()

      userLocalSource.saveUser(user.toUserLocal(baseUrl))
    }
  }

  private suspend fun uploadAvatar(avatar: Uri?): Long? {
    return withContext(dispatchers.io) {
      if (avatar != null) {
        val contentResolver = application.contentResolver
        val type = contentResolver.getType(avatar)!!
        val inputStream = contentResolver.openInputStream(avatar)!!
        val fileName = contentResolver.query(avatar, null, null, null, null)!!.use {
          it.moveToFirst()
          it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
        val bytes = ByteArrayOutputStream().use {
          inputStream.copyTo(it)
          it.toByteArray()
        }

        val requestFile = bytes.toRequestBody(type.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

        apiService.uploadFile(body).unwrap().id
      } else {
        null
      }
    }
  }

  override suspend fun login(phone: String, password: String): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        val deviceToken = firebaseInstanceId.instanceId.await().token
        Timber.d("Device token: $deviceToken")

        val (token, user) = apiService.loginUser(
          LoginUserBody(
            phone = phone,
            password = password,
            deviceToken = deviceToken,
          )
        ).unwrap()

        userLocalSource.saveToken(token)
        userLocalSource.saveUser(user.toUserLocal(baseUrl))
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
              birthday = birthday?.toString_yyyyMMdd(UTCTimeZone),
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
      val userId = userLocalSource.user()?.id
        ?: return userLocalSource.removeUserAndToken()

      apiService
        .getUserDetail(userId)
        .unwrap()
        .toUserLocal(baseUrl)
        .let { userLocalSource.saveUser(it) }

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
    const val CHECK_AUTH_INTERVAL = 180_000L // 3 minutes
  }
}