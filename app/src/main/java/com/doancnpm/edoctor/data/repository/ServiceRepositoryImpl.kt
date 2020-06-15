package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.toServiceDomain
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.domain.repository.ServiceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

class ServiceRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val baseUrl: String,
) : ServiceRepository {
  override suspend fun getServicesByCategory(
    category: Category,
    page: Int,
    perPage: Int
  ): DomainResult<List<Service>> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        apiService
          .getServicesByCategory(
            page = page,
            perPage = perPage,
            categoryId = category.id
          )
          .unwrap()
          .services
          .map { it.toServiceDomain(baseUrl) }
          .also { delay(200) }
          .also { Timber.d("getServicesByCategory { page: $page, perPage: $perPage, categoryId: ${category.id} }") }
      }
    }
  }
}
