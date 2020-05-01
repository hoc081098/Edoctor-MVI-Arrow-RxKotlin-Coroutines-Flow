package com.doancnpm.edoctor.utils

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private inline fun <T> SharedPreferences.delegate(
  crossinline getter: SharedPreferences.(key: String, defaultValue: T) -> T,
  crossinline setter: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor,
  defaultValue: T,
  key: String? = null,
): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
  override fun getValue(thisRef: Any, property: KProperty<*>) =
    getter(key ?: property.name, defaultValue)

  override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
    edit().setter(key ?: property.name, value).apply()
}

/**
 * Valid type: `String?`, `Set<*>?`, `Boolean`, `Int`, `Long`, `Float`
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any?> SharedPreferences.delegate(
  defaultValue: T,
  key: String? = null
): ReadWriteProperty<Any, T> {
  return when (defaultValue) {
    is String? -> delegate(
      SharedPreferences::getString,
      SharedPreferences.Editor::putString,
      defaultValue,
      key,
    )
    is Set<*>? -> delegate(
      SharedPreferences::getStringSet,
      SharedPreferences.Editor::putStringSet,
      defaultValue?.filterIsInstanceTo(mutableSetOf<String>()),
      key,
    )
    is Boolean -> delegate(
      SharedPreferences::getBoolean,
      SharedPreferences.Editor::putBoolean,
      defaultValue,
      key,
    )
    is Int -> delegate(
      SharedPreferences::getInt,
      SharedPreferences.Editor::putInt,
      defaultValue,
      key,
    )
    is Long -> delegate(
      SharedPreferences::getLong,
      SharedPreferences.Editor::putLong,
      defaultValue,
      key,
    )
    is Float -> delegate(
      SharedPreferences::getFloat,
      SharedPreferences.Editor::putFloat,
      defaultValue,
      key,
    )
    else -> {
      val clazz = (defaultValue ?: error("Cannot determine type of null value"))::class.java
      error("Not support for type $clazz")
    }
  } as ReadWriteProperty<Any, T>
}