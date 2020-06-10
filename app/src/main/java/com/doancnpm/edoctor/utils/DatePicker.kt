package com.doancnpm.edoctor.utils

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Looper
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.doancnpm.edoctor.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.android.MainThreadDisposable.verifyMainThread
import io.reactivex.rxjava3.core.Maybe
import timber.log.Timber
import java.util.*
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener as OnPositiveButtonClickListener

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
}

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