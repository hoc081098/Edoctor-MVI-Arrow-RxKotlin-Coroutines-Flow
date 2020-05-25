package com.doancnpm.edoctor.domain.repository

import arrow.core.Option
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.User
import io.reactivex.rxjava3.core.Observable
import java.util.*

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
}