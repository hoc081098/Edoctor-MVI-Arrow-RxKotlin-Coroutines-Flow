package com.doancnpm.edoctor.utils

import kotlinx.coroutines.delay
import java.io.IOException

suspend fun <T> retryIO(
  times: Int,
  initialDelay: Long,
  factor: Double,
  maxDelay: Long = Long.MAX_VALUE,
  block: suspend () -> T,
): T {
  var currentDelay = initialDelay
  repeat(times - 1) {
    try {
      return block()
    } catch (e: IOException) {
      // you can log an error here and/or make a more finer-grained
      // analysis of the cause to see if retry is needed
    }
    delay(currentDelay)
    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
  }
  return block() // last attempt
}
