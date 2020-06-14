package com.doancnpm.edoctor.domain.repository

import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Promotion

interface PromotionRepository {
  suspend fun getPromotions(): DomainResult<List<Promotion>>
}