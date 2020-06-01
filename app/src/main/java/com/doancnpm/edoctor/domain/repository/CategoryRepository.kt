package com.doancnpm.edoctor.domain.repository

import androidx.annotation.IntRange
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.DomainResult

interface CategoryRepository {
  suspend fun getCategories(
    @IntRange(from = 1) page: Int,
    @IntRange(from = 1) perPage: Int,
  ): DomainResult<List<Category>>
}