package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.toNotificationDomain
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Notification
import com.doancnpm.edoctor.domain.repository.NotificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

class NotificationRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val dispatchers: AppDispatchers,
  private val baseUrl: String,
) : NotificationRepository {
  override suspend fun getNotifications(page: Int, perPage: Int): DomainResult<List<Notification>> {
    return Either.catch(errorMapper::map) {
      withContext(dispatchers.io) {
        apiService
          .getNotifications(
            page = page,
            perPage = perPage,
          )
          .unwrap()
          .notifications
          .map { it.toNotificationDomain(baseUrl) }
          .also { delay(200) }
          .also { Timber.d("getNotifications { page: $page, perPage: $perPage }") }
      }
    }
  }
}
