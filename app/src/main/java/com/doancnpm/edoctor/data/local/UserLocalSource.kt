package com.doancnpm.edoctor.data.local

import com.doancnpm.edoctor.data.local.model.UserLocal
import com.doancnpm.edoctor.domain.entity.Option
import io.reactivex.rxjava3.core.Observable

interface UserLocalSource {
  /*
  * Token
  */

  suspend fun token(): String?

  suspend fun saveToken(token: String)

  fun tokenObservable(): Observable<Option<String>>

  /*
   * User
   */

  suspend fun user(): UserLocal?

  suspend fun saveUser(user: UserLocal)

  fun userObservable(): Observable<Option<UserLocal>>

  suspend fun removeUserAndToken()
}