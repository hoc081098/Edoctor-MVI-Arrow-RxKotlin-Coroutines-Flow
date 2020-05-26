package com.doancnpm.edoctor.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Looper
import android.widget.DatePicker
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.*

fun Context.pickDateObservable(year: Int, month: Int, dayOfMonth: Int): Observable<Date> {
  return PickDateObservable(
    this,
    year,
    month,
    dayOfMonth,
  )
}

private class PickDateObservable(
  private val context: Context,
  private val year: Int,
  private val month: Int,
  private val dayOfMonth: Int,
) : Observable<Date>() {
  override fun subscribeActual(observer: Observer<in Date>) {

    if (Looper.myLooper() != Looper.getMainLooper()) {
      observer.onSubscribe(Disposable.empty())
      observer.onError(IllegalStateException(
        "Expected to be called on the main thread but was " + Thread.currentThread().name))
      return
    }

    val dialog = DatePickerDialog(
      context,
      android.R.style.Theme_Material_Light_Dialog_Alert,
      null,
      year,
      month,
      dayOfMonth,
    ).apply { show() }

    val listener = Listener(dialog, observer)
    observer.onSubscribe(listener)

    dialog.setOnCancelListener(listener)
    dialog.setOnDateSetListener(listener)
  }

  private class Listener(
    private val dialog: DatePickerDialog,
    private val observer: Observer<in Date>,
  ) : MainThreadDisposable(),
    DialogInterface.OnCancelListener,
    DatePickerDialog.OnDateSetListener {

    override fun onDispose() {
      Timber.d("[onDispose]")

      dialog.setOnCancelListener(null)
      dialog.setOnDateSetListener(null)
      dialog.dismiss()
    }

    override fun onCancel(dialog: DialogInterface?) {
      if (!isDisposed) {
        Timber.d("[onCancel]")
        observer.onComplete()
      }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
      if (!isDisposed) {
        val date = Calendar.getInstance(Locale.getDefault())
          .apply { set(year, month, dayOfMonth, 0, 0, 0) }
          .time
          .also { Timber.d("[onDateSet] $it") }

        observer.onNext(date)
        observer.onComplete()
      }
    }
  }
}