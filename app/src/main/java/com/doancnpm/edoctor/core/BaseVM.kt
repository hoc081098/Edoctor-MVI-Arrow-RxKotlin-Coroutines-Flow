package com.doancnpm.edoctor.core

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

open class BaseVM : ViewModel() {
  protected val compositeDisposable = CompositeDisposable()

  init {
    @Suppress("LeakingThis")
    Timber.d("$this::init")
  }

  @CallSuper
  override fun onCleared() {
    super.onCleared()
    compositeDisposable.dispose()
    Timber.d("$this::onCleared")
  }
}