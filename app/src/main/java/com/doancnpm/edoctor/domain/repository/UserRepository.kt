package com.doancnpm.edoctor.domain.repository

import arrow.core.Option
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.User
import io.reactivex.Observable

interface UserRepository {
  suspend fun login(email: String, password: String): DomainResult<Unit>

  fun userObservable(): Observable<DomainResult<Option<User>>>

  suspend fun checkAuth(): DomainResult<Boolean>
}