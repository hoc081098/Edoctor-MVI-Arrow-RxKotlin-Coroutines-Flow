package com.doancnpm.edoctor.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import arrow.core.toOption
import com.doancnpm.edoctor.data.local.model.UserLocal
import com.doancnpm.edoctor.data.local.model.UserLocalJsonAdapter
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import kotlinx.coroutines.withContext

class UserLocalSourceImpl(
  rxkPrefs: RxkPrefs,
  private val sharedPreferences: SharedPreferences,
  private val dispatchers: AppDispatchers,
  private val userLocalJsonAdapter: UserLocalJsonAdapter,
) : UserLocalSource {

  private val tokenPref = rxkPrefs.string(TOKEN_KEY)
  private val userPref = rxkPrefs.string(USER_KEY)

  private val tokenObservable = tokenPref.observe()
    .map { it.nullIfBlank().toOption() }
    .replay(1)
    .refCount()

  private val userObservable = userPref.observe()
    .map { it.toUserLocal().toOption() }
    .replay(1)
    .refCount()

  override fun token() = tokenPref.get().nullIfBlank()

  override suspend fun saveToken(token: String) {
    withContext(dispatchers.io) {
      sharedPreferences.edit(commit = true) {
        putString(TOKEN_KEY, token)
      }
    }
  }

  override fun tokenObservable() = tokenObservable

  override suspend fun saveUser(user: UserLocal) {
    withContext(dispatchers.io) {
      sharedPreferences.edit(commit = true) {
        putString(USER_KEY, userLocalJsonAdapter.toJson(user))
      }
    }
  }

  override suspend fun user(): UserLocal? {
    return withContext(dispatchers.io) { userPref.get().toUserLocal() }
  }

  override fun userObservable() = userObservable

  override suspend fun removeUserAndToken() {
    withContext(dispatchers.io) {
      sharedPreferences.edit(commit = true) {
        putString(TOKEN_KEY, "")
        putString(USER_KEY, "")
      }
    }
  }

  private fun String.toUserLocal() =
    kotlin.runCatching { userLocalJsonAdapter.fromJson(this) }
      .getOrNull()

  private companion object {
    fun String.nullIfBlank() = if (this.isBlank()) null else this

    const val TOKEN_KEY = "com.doancnpm.edoctor.token"
    const val USER_KEY = "com.doancnpm.edoctor.user"
  }
}