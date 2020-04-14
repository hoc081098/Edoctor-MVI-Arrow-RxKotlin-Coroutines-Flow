package com.doancnpm.edoctor.domain.repository

import com.doancnpm.edoctor.domain.entity.DomainResult

interface UserRepository {
  fun login(email: String, password: String): DomainResult<Unit>
}