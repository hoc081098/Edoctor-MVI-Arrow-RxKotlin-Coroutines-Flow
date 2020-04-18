package com.doancnpm.edoctor.core

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

open class BaseFragment : Fragment() {
  protected val compositeDisposable = CompositeDisposable()

  override fun onDestroyView() {
    super.onDestroyView()
    compositeDisposable.clear()
  }
}