package com.doancnpm.edoctor.domain.repository

import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.DomainResult

interface CardRepository {
  suspend fun addCard(card: Card): DomainResult<Unit>
}