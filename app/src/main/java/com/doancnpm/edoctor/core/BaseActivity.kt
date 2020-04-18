package com.doancnpm.edoctor.core

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
  protected val compositeDisposable = CompositeDisposable()

  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.clear()
  }
}