package com.doancnpm.edoctor.domain.repository

import android.net.Uri
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Option
import com.doancnpm.edoctor.domain.entity.User
import io.reactivex.rxjava3.core.Observable
import java.util.Date

interface UserRepository {
  suspend fun login(phone: String, password: String): DomainResult<Unit>

  /**
   * @return phone if register successfully, otherwise returns app error
   */
  suspend fun register(
    phone: String,
    password: String,
    roleId: User.RoleId,
    fullName: String,
    birthday: Date?,
  ): DomainResult<String>

  fun userObservable(): Observable<DomainResult<Option<User>>>

  suspend fun checkAuth(): DomainResult<Boolean>

  suspend fun logout(): DomainResult<Unit>

  suspend fun resendCode(phone: String): DomainResult<Unit>

  suspend fun verify(phone: String, code: String): DomainResult<Unit>

  suspend fun updateUserProfile(
    fullName: String,
    birthday: Date?,
    avatar: Uri?
  ): DomainResult<Unit>
}