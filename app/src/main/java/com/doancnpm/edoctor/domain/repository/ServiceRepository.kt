package com.doancnpm.edoctor.domain.repository

import androidx.annotation.IntRange
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Service

interface ServiceRepository {
  suspend fun getServicesByCategory(
    category: Category,
    @IntRange(from = 1) page: Int,
    @IntRange(from = 1) perPage: Int,
  ): DomainResult<List<Service>>
}