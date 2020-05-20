package com.doancnpm.edoctor.utils

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

class RxSharedPreferences(private val sharedPreferences: SharedPreferences) {
  private val keySubject = PublishSubject.create<String>()
    .apply { subscribeBy { Timber.d(">>>>>>> key: $it") } }

  private val listener =
    OnSharedPreferenceChangeListener { _, key -> keySubject.onNext(key) }
      .also { sharedPreferences.registerOnSharedPreferenceChangeListener(it) }

  /**
   * Valid type of [defValue]: `String?`, `Set<*>?`, `Boolean`, `Int`, `Long`, `Float`
   */
  @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
  private inline fun <reified T : Any> observe(key: String, defValue: T?): Observable<Option<T>> {
    return keySubject
      .filter { it == key }
      .map { Unit }
      .startWithItem(Unit)
      .map {
        (when (val clazz = T::class) {
          String::class -> sharedPreferences.getString(key, defValue as String?)
          Set::class -> sharedPreferences.getStringSet(key,
            (defValue as Set<*>?)?.filterIsInstanceTo(LinkedHashSet()))
          Boolean::class -> sharedPreferences.getBoolean(key, defValue as Boolean)
          Int::class -> sharedPreferences.getInt(key, defValue as Int)
          Long::class -> sharedPreferences.getLong(key, defValue as Long)
          Float::class -> sharedPreferences.getFloat(key, defValue as Float)
          else -> error("Not support for type $clazz")
        } as T?).toOption()
      }
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun <T> Observable<Option<T>>.unwrap(defValue: T): Observable<T> =
    map { it.getOrElse { defValue } }

  fun observeString(key: String, defValue: String? = null): Observable<Option<String>> =
    observe(key, defValue)

  fun observeStringSet(
    key: String,
    defValue: Set<String>? = null,
  ): Observable<Option<Set<String>>> =
    observe(key, defValue)

  fun observeBoolean(key: String, defValue: Boolean = false): Observable<Boolean> =
    observe(key, defValue).unwrap(defValue)

  fun observeInt(key: String, defValue: Int = 0): Observable<Int> =
    observe(key, defValue).unwrap(defValue)

  fun observeLong(key: String, defValue: Long = 0L): Observable<Long> =
    observe(key, defValue).unwrap(defValue)

  fun observeFloat(key: String, defValue: Float = 0f): Observable<Float> =
    observe(key, defValue).unwrap(defValue)

  fun close() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    keySubject.onComplete()
  }
}