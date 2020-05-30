@file:Suppress("FunctionName")

package com.doancnpm.edoctor.utils

import java.text.SimpleDateFormat
import java.util.*

private val dateFormatter get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

fun parse_yyyyMMdd(source: String): Date? = dateFormatter.parse(source)

fun Date.toString_yyyyMMdd(): String = dateFormatter.format(this)
