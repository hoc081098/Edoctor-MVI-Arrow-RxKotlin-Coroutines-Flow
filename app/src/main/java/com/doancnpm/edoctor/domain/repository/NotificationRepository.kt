package com.doancnpm.edoctor.domain.repository

import androidx.annotation.IntRange
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Notification

interface NotificationRepository {
  suspend fun getNotifications(
    @IntRange(from = 1) page: Int,
    @IntRange(from = 1) perPage: Int,
  ): DomainResult<List<Notification>>
}