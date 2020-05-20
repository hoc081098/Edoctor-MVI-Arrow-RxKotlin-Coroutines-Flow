package com.doancnpm.edoctor.utils

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import arrow.core.Option
import arrow.core.toOption
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber
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

/**
 * Valid type of [defaultValue]: `String?`, `Set<*>?`, `Boolean`, `Int`, `Long`, `Float`
 */
@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
fun <T : Any> SharedPreferences.observe(defaultValue: T?, key: String): Observable<Option<T>> {
  /**
   * @throws IllegalStateException
   */
  fun getValue(): T? = when (defaultValue) {
    is String? -> getString(key, defaultValue)
    is Set<*>? -> getStringSet(key, defaultValue?.filterIsInstanceTo(mutableSetOf<String>()))
    is Boolean? -> getBoolean(key, defaultValue ?: false)
    is Int? -> getInt(key, defaultValue ?: 0)
    is Long? -> getLong(key, defaultValue ?: 0L)
    is Float? -> getFloat(key, defaultValue ?: 0f)
    else -> {
      val clazz = (defaultValue ?: error("Cannot determine type of null value"))::class.java
      error("Not support for type $clazz")
    }
  } as T?

  return Observable.create { emitter ->
    fun emit() = runCatching { getValue().toOption() }
      .onSuccess(emitter::onNext)
      .onFailure(emitter::tryOnError)

    val listener = OnSharedPreferenceChangeListener { _, changedKey ->
      if (changedKey == key) emit()
    }
    registerOnSharedPreferenceChangeListener(listener)
    emitter.setCancellable {
      unregisterOnSharedPreferenceChangeListener(listener)
      Timber.d("Remove listener")
    }

    emit() // emit seeded value
  }
}