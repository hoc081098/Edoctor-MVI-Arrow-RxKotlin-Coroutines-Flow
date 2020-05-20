package com.doancnpm.edoctor.utils

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private inline fun <T> SharedPreferences.delegate(
  crossinline getter: SharedPreferences.(key: String, defaultValue: T) -> T,
  crossinline setter: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor,
  defaultValue: T,
  key: String? = null,
  commit: Boolean = false,
): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
  override fun getValue(thisRef: Any, property: KProperty<*>) =
    getter(key ?: property.name, defaultValue)

  override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
    edit(commit) { setter(key ?: property.name, value) }
}

/**
 * Valid type: `String?`, `Set<*>?`, `Boolean`, `Int`, `Long`, `Float`
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any?> SharedPreferences.delegate(
  defaultValue: T,
  key: String? = null,
  commit: Boolean = false,
): ReadWriteProperty<Any, T> {
  return when (defaultValue) {
    is String? -> delegate(
      SharedPreferences::getString,
      SharedPreferences.Editor::putString,
      defaultValue,
      key,
      commit,
    )
    is Set<*>? -> delegate(
      SharedPreferences::getStringSet,
      SharedPreferences.Editor::putStringSet,
      defaultValue?.filterIsInstanceTo(mutableSetOf<String>()),
      key,
      commit,
    )
    is Boolean -> delegate(
      SharedPreferences::getBoolean,
      SharedPreferences.Editor::putBoolean,
      defaultValue,
      key,
      commit,
    )
    is Int -> delegate(
      SharedPreferences::getInt,
      SharedPreferences.Editor::putInt,
      defaultValue,
      key,
      commit,
    )
    is Long -> delegate(
      SharedPreferences::getLong,
      SharedPreferences.Editor::putLong,
      defaultValue,
      key,
      commit,
    )
    is Float -> delegate(
      SharedPreferences::getFloat,
      SharedPreferences.Editor::putFloat,
      defaultValue,
      key,
      commit,
    )
    else -> {
      val clazz = (defaultValue ?: error("Cannot determine type of null value"))::class.java
      error("Not support for type $clazz")
    }
  } as ReadWriteProperty<Any, T>
}

private val listeners = ConcurrentHashMap<ObservableEmitter<*>, OnSharedPreferenceChangeListener>()

/**
 * Valid type of [defValue]: `String?`, `Set<*>?`, `Boolean`, `Int`, `Long`, `Float`
 */
@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
private inline fun <reified T : Any> SharedPreferences.observe(
  key: String,
  defValue: T?
): Observable<Option<T>> {
  return Observable
    .create<Unit> { emitter ->
      registerOnSharedPreferenceChangeListener(
        OnSharedPreferenceChangeListener { _, changedKey ->
          if (changedKey == key) {
            emitter.onNext(Unit)
          }
        }.also { listeners[emitter] = it }
      )
      emitter.setCancellable {
        unregisterOnSharedPreferenceChangeListener(
          listeners
            .remove(emitter)
            .also { Timber.d("Remove listener $it. Listeners count: ${listeners.size}") }
        )
      }
    }
    .startWithItem(Unit)
    .map {
      (when (val clazz = T::class) {
        String::class -> getString(key, defValue as String?)
        Set::class -> getStringSet(key, (defValue as Set<*>?)?.filterIsInstanceTo(LinkedHashSet()))
        Boolean::class -> getBoolean(key, defValue as Boolean)
        Int::class -> getInt(key, defValue as Int)
        Long::class -> getLong(key, defValue as Long)
        Float::class -> getFloat(key, defValue as Float)
        else -> error("Not support for type $clazz")
      } as T?).toOption()
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <T> Observable<Option<T>>.unwrap(defValue: T): Observable<T> =
  map { it.getOrElse { defValue } }

fun SharedPreferences.observeString(
  key: String,
  defValue: String? = null,
): Observable<Option<String>> = observe(key, defValue)

fun SharedPreferences.observeStringSet(
  key: String,
  defValue: Set<String>? = null,
): Observable<Option<Set<String>>> = observe(key, defValue)

fun SharedPreferences.observeBoolean(key: String, defValue: Boolean = false): Observable<Boolean> =
  observe(key, defValue).unwrap(defValue)

fun SharedPreferences.observeInt(key: String, defValue: Int = 0): Observable<Int> =
  observe(key, defValue).unwrap(defValue)

fun SharedPreferences.observeLong(key: String, defValue: Long = 0L): Observable<Long> =
  observe(key, defValue).unwrap(defValue)

fun SharedPreferences.observeFloat(key: String, defValue: Float = 0f): Observable<Float> =
  observe(key, defValue).unwrap(defValue)