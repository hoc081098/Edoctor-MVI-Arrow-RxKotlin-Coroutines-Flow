package com.doancnpm.edoctor.utils

import androidx.annotation.CheckResult
import arrow.core.Some
import arrow.core.toOption
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.jakewharton.rxrelay2.Relay
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.annotations.SchedulerSupport
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.Subject

@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
inline fun <reified U : Any, T : Any> Observable<T>.notOfType() = filter { it !is U }!!

@Suppress("nothing_to_inline")
inline fun <T : Any> Relay<T>.asObservable(): Observable<T> = this

@Suppress("nothing_to_inline")
inline fun <T : Any> Subject<T>.asObservable(): Observable<T> = this

@CheckResult
inline fun <T : Any, R : Any> Observable<T>.exhaustMap(crossinline transform: (T) -> Observable<R>): Observable<R> {
  return this
    .toFlowable(BackpressureStrategy.DROP)
    .flatMap({ transform(it).toFlowable(BackpressureStrategy.MISSING) }, 1)
    .toObservable()
}

@CheckResult
inline fun <T : Any, R : Any> Observable<T>.mapNotNull(crossinline transform: (T) -> R?): Observable<R> {
  return map { transform(it).toOption() }
    .ofType<Some<R>>()
    .map { it.t }
}

@Suppress("NOTHING_TO_INLINE")
@CheckResult
inline fun <T> Observable<DomainResult<T>>.catchError(errorMapper: ErrorMapper): Observable<DomainResult<T>> {
  return onErrorReturn { errorMapper.mapAsLeft(it) }
}