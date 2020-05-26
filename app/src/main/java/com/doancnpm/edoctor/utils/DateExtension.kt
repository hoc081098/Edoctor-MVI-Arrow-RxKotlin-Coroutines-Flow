@file:Suppress("FunctionName")

package com.doancnpm.edoctor.utils

import java.text.SimpleDateFormat
import java.util.*


private val dateFormatter by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

fun parse_yyyyMMdd(source: String) = dateFormatter.parse(source)

fun Date.toString_yyyyMMdd() = dateFormatter.format(this)
