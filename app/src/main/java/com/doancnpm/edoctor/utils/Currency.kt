package com.doancnpm.edoctor.utils

import java.text.NumberFormat
import java.util.*

private val localeVi = Locale("vi", "VN")
private val formatter = NumberFormat.getCurrencyInstance(localeVi).apply {
  currency = Currency.getInstance(localeVi)
  maximumFractionDigits = 0
}

val Int.currencyVndFormatted get() = formatter.format(toLong())