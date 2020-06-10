package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.toPromotionDomain
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Promotion
import com.doancnpm.edoctor.domain.repository.PromotionRepository

class PromotionRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
) : PromotionRepository {
  override suspend fun getPromotions(): DomainResult<List<Promotion>> {
    return Either.catch(errorMapper::map) {
      apiService
        .getPromotions(page = 1, perPage = Int.MAX_VALUE)
        .unwrap()
        .promotions
        .map { it.toPromotionDomain() }
    }
  }
}

