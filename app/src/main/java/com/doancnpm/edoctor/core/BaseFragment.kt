package com.doancnpm.edoctor.core

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

open class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
  protected val compositeDisposable = CompositeDisposable()

  override fun onDestroyView() {
    super.onDestroyView()
    compositeDisposable.clear()
  }
}