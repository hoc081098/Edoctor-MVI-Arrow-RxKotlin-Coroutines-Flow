package com.doancnpm.edoctor.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.doancnpm.edoctor.R
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.android.MainThreadDisposable.verifyMainThread
import io.reactivex.rxjava3.core.Maybe
import java.util.*
import java.util.Calendar.*

/**
 * @param initialHourOfDay the initial hour
 * @param initialMinute the initial minute
 * @return a [Maybe] that completes with a [Pair] of hourOfDay and minute
 */
fun Context.pickTimeObservable(
  initialHourOfDay: Int,
  initialMinute: Int,
): Maybe<Pair<Int, Int>> {
  return Maybe.create { emitter ->
    verifyMainThread()

    val dialog = TimePickerDialog(
      this,
      R.style.AppTheme_AlertDialog,
      { _, hourOfDay, minute -> emitter.onSuccess(hourOfDay to minute) },
      initialHourOfDay,
      initialMinute,
      true,
    ).apply { show() }

    emitter.setDisposable(object : MainThreadDisposable() {
      override fun onDispose() = dialog.dismiss()
    })
  }
}

fun Context.pickDateObservable(initialSelection: Date?): Maybe<Date> {
  return Maybe.create { emitter ->
    verifyMainThread()

    val calendar = getInstance().apply { time = initialSelection ?: Date() }

    val dialog = DatePickerDialog(
      this,
      R.style.AppTheme_AlertDialog,
      { view, year, month, dayOfMonth ->
        emitter.onSuccess(
          calendar.apply {
            this[YEAR] = year
            this[MONTH] = month
            this[DAY_OF_MONTH] = dayOfMonth
          }.time
        )
      },
      calendar[YEAR],
      calendar[MONTH],
      calendar[DAY_OF_MONTH],
    ).apply { show() }

    emitter.setDisposable(object : MainThreadDisposable() {
      override fun onDispose() {
        dialog.setOnDateSetListener(null)
        dialog.dismiss()
      }
    })
  }
}

/*
private fun FragmentActivity.materialDatePicker(
  initialSelection: Date?,
  validator: DateValidator?,
): MaterialDatePicker<Long> {
  return MaterialDatePicker.Builder
    .datePicker()
    .setSelection(initialSelection?.time)
    .setInputMode(INPUT_MODE_CALENDAR)
    .setCalendarConstraints(
      CalendarConstraints.Builder()
        .apply {
          validator?.let(::setValidator)
        }
        .build()
    )
    .build()
    .apply { show(supportFragmentManager, toString()) }
}

fun FragmentActivity.pickDateObservable(
  initialSelection: Date?,
  validator: DateValidator? = null,
): Maybe<Date> {
  return Maybe.create { emitter ->
    if (Looper.myLooper() != Looper.getMainLooper()) {
      emitter.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
      return@create
    }

    val datePicker = materialDatePicker(initialSelection, validator)

    val onPositiveButtonClickListener = OnPositiveButtonClickListener<Long> { selection ->
      selection ?: return@OnPositiveButtonClickListener emitter.onComplete()

      val date = Date(selection).also { Timber.d("[onDateSet] $it") }
      emitter.onSuccess(date)
      emitter.onComplete()
    }
    val onCancelListener = DialogInterface.OnCancelListener {
      Timber.d("[onCancel]")
      emitter.onComplete()
    }
    val onNegativeButtonClickListener = View.OnClickListener {
      Timber.d("[onClickCancel]")
      emitter.onComplete()
    }

    datePicker.addOnPositiveButtonClickListener(onPositiveButtonClickListener)
    datePicker.addOnCancelListener(onCancelListener)
    datePicker.addOnNegativeButtonClickListener(onNegativeButtonClickListener)

    emitter.setDisposable(object : MainThreadDisposable() {
      override fun onDispose() {

        datePicker.removeOnPositiveButtonClickListener(onPositiveButtonClickListener)
        datePicker.removeOnCancelListener(onCancelListener)
        datePicker.removeOnNegativeButtonClickListener(onNegativeButtonClickListener)
        runCatching { datePicker.dismissAllowingStateLoss() }

        Timber.d("[onDispose]")
      }
    })
  }
}*/