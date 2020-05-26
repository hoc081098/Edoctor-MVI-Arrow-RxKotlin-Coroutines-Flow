package com.doancnpm.edoctor.utils

import arrow.core.Some
import arrow.core.toOption
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.jakewharton.rxrelay3.Relay
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.annotations.SchedulerSupport
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.ofType
import io.reactivex.rxjava3.subjects.Subject

@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
inline fun <reified U : Any, T : Any> Observable<T>.notOfType() = filter { it !is U }!!

@Suppress("nothing_to_inline")
inline fun <T : Any> Relay<T>.asObservable(): Observable<T> = this

@Suppress("nothing_to_inline")
inline fun <T : Any> Subject<T>.asObservable(): Observable<T> = this

@CheckReturnValue
inline fun <T : Any, R : Any> Observable<T>.exhaustMap(crossinline transform: (T) -> Observable<R>): Observable<R> {
  return this
    .toFlowable(BackpressureStrategy.DROP)
    .flatMap({ transform(it).toFlowable(BackpressureStrategy.MISSING) }, 1)
    .toObservable()
}

@CheckReturnValue
inline fun <T : Any, R : Any> Observable<T>.mapNotNull(crossinline transform: (T) -> R?): Observable<R> {
  return map { transform(it).toOption() }
    .ofType<Some<R>>()
    .map { it.t }
}

@Suppress("NOTHING_TO_INLINE")
@CheckReturnValue
inline fun <T> Observable<DomainResult<T>>.catchError(errorMapper: ErrorMapper): Observable<DomainResult<T>> {
  return onErrorReturn { errorMapper.mapAsLeft(it) }
}