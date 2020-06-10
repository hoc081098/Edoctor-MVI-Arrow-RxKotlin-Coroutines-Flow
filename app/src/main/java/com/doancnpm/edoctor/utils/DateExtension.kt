@file:Suppress("FunctionName")

package com.doancnpm.edoctor.utils

import java.text.SimpleDateFormat
import java.util.*

@JvmField
val UTCTimeZone = TimeZone.getTimeZone("UTC")!!

fun Date.toString_yyyyMMdd(zone: TimeZone = TimeZone.getDefault()): String =
  SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    .apply { timeZone = zone }
    .format(this)

fun Date.toString_yyyyMMdd_HHmmss(zone: TimeZone = TimeZone.getDefault()): String =
  SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    .apply { timeZone = zone }
    .format(this)
