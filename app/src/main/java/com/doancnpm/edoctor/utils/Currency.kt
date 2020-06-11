package com.doancnpm.edoctor.utils

import android.icu.text.NumberFormat
import android.icu.util.Currency
import java.util.*

private val localeVi = Locale("vi", "VN")
private val formatter = NumberFormat.getCurrencyInstance(localeVi).apply {
  currency = Currency.getInstance(localeVi)
  maximumFractionDigits = 0
}

val Int.currencyVndFormatted: String get() = formatter.format(toLong())

val Double.currencyVndFormatted: String get() = formatter.format(this)