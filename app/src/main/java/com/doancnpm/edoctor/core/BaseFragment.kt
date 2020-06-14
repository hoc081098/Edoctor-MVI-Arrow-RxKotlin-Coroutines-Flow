package com.doancnpm.edoctor.core

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber

open class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
  protected val compositeDisposable = CompositeDisposable()

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Timber.d("$this::onCreate: $savedInstanceState")
  }

  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Timber.d("$this::onViewCreated: $view, $savedInstanceState")
  }

  @CallSuper
  override fun onDestroyView() {
    super.onDestroyView()
    compositeDisposable.clear()
    Timber.d("$this::onDestroyView")
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()
    Timber.d("$this::onDestroy")
  }
}