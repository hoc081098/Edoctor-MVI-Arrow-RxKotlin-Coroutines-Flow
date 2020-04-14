package com.doancnpm.edoctor.data.local

import arrow.core.Option
import com.doancnpm.edoctor.data.local.model.UserLocal
import io.reactivex.Observable

interface UserLocalSource {
  /*
  * Token
  */

  fun token(): String?

  suspend fun saveToken(token: String)

  fun tokenObservable(): Observable<Option<String>>

  /*
   * User
   */

  suspend fun user(): UserLocal?

  suspend fun saveUser(user: UserLocal)

  fun userObservable(): Observable<Option<UserLocal>>
}