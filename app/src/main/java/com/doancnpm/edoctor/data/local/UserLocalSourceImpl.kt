package com.doancnpm.edoctor.data.local

import android.content.SharedPreferences
import com.doancnpm.edoctor.data.local.model.UserLocal
import com.doancnpm.edoctor.data.local.model.UserLocalJsonAdapter
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.utils.delegate
import com.doancnpm.edoctor.utils.observeString
import kotlinx.coroutines.withContext

class UserLocalSourceImpl(
  sharedPreferences: SharedPreferences,
  private val dispatchers: AppDispatchers,
  private val userLocalJsonAdapter: UserLocalJsonAdapter,
  private val schedulers: AppSchedulers,
) : UserLocalSource {

  private var token by sharedPreferences.delegate(null as String?, TOKEN_KEY, commit = true)
  private var userLocal by sharedPreferences.delegate(null as String?, USER_KEY, commit = true)

  private val tokenObservable = sharedPreferences.observeString(TOKEN_KEY)
    .subscribeOn(schedulers.io)
    .replay(1)
    .refCount()!!

  private val userObservable = sharedPreferences.observeString(USER_KEY)
    .subscribeOn(schedulers.io)
    .map { json -> json.mapNotNull { it.toUserLocal() } }
    .replay(1)
    .refCount()!!

  override suspend fun token() = withContext(dispatchers.io) { token }

  override suspend fun saveToken(token: String) =
    withContext(dispatchers.io) { this@UserLocalSourceImpl.token = token }

  override fun tokenObservable() = tokenObservable

  override suspend fun saveUser(user: UserLocal) = withContext(dispatchers.io) {
    this@UserLocalSourceImpl.userLocal = userLocalJsonAdapter.toJson(user)
  }

  override suspend fun user() = withContext(dispatchers.io) { userLocal.toUserLocal() }

  override fun userObservable() = userObservable

  override suspend fun removeUserAndToken() = withContext(dispatchers.io) {
    userLocal = null
    token = null
  }

  private fun String?.toUserLocal(): UserLocal? =
    runCatching { userLocalJsonAdapter.fromJson(this ?: return null) }.getOrNull()

  private companion object {
    const val TOKEN_KEY = "com.doancnpm.edoctor.token"
    const val USER_KEY = "com.doancnpm.edoctor.user"
  }
}