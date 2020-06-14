package com.doancnpm.edoctor.domain.repository

import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.DomainResult
import kotlinx.coroutines.flow.Flow

interface CardRepository {
  suspend fun addCard(
    holderName: String,
    number: String,
    expiredMonth: Int,
    expiredYear: Int,
    cvc: Int,
  ): DomainResult<Unit>

  suspend fun removeCard(id: String): DomainResult<Unit>

  fun getCards(): Flow<DomainResult<List<Card>>>
}