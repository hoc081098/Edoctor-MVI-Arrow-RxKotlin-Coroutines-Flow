package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.toCategoryDomain
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.repository.CategoryRepository
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val baseUrl: String,
) : CategoryRepository {
  override suspend fun getCategories(page: Int, perPage: Int): DomainResult<List<Category>> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        apiService
          .getCategories(page = page, perPage = perPage)
          .unwrap()
          .categories
          .map { it.toCategoryDomain(baseUrl) }
      }
    }
  }
}
