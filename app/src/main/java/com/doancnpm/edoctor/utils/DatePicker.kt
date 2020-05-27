package com.doancnpm.edoctor.utils

import android.content.DialogInterface
import android.os.Looper
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.core.Maybe
import timber.log.Timber
import java.util.*
import java.util.Calendar.*
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener as OnPositiveButtonClickListener

fun FragmentActivity.pickDateObservable(
  year: Int,
  month: Int,
  dayOfMonth: Int,
  validator: DateValidator? = null,
): Maybe<Date> {
  return Maybe.create { emitter ->
    if (Looper.myLooper() != Looper.getMainLooper()) {
      emitter.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
      return@create
    }

    val datePicker = materialDatePicker(year, month, dayOfMonth, validator)

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

private fun FragmentActivity.materialDatePicker(
  year: Int,
  month: Int,
  dayOfMonth: Int,
  validator: DateValidator?,
): MaterialDatePicker<Long> {
  val initialSelection = getInstance(Locale.getDefault())
    .apply {
      this[YEAR] = year
      this[MONTH] = month
      this[DAY_OF_MONTH] = dayOfMonth
    }
    .timeInMillis

  val datePicker = MaterialDatePicker.Builder
    .datePicker()
    .setSelection(initialSelection)
    .setInputMode(INPUT_MODE_CALENDAR)
    .setCalendarConstraints(
      CalendarConstraints.Builder()
        .apply {
          validator?.let(::setValidator)
        }
        .build()
    )
    .build()
    .apply { show(supportFragmentManager, this.toString()) }
  return datePicker
}