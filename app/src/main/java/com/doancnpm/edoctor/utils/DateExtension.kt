@file:Suppress("FunctionName")

package com.doancnpm.edoctor.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.toString_yyyyMMdd(zone: TimeZone): String =
  SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    .apply { timeZone = zone }
    .format(this)
