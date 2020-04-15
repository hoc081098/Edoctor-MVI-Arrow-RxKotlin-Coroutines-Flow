package com.doancnpm.edoctor.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T : Any>(private val content: T) {

  var hasBeenHandled = false
    private set // Allow external read but not write

  /**
   * Returns the content and prevents its use again.
   */
  fun getContentIfNotHandled(): T? {
    return if (hasBeenHandled) {
      null
    } else {
      hasBeenHandled = true
      content
    }
  }

  /**
   * Returns the content, even if it's already been handled.
   */
  fun peekContent(): T = content
}

inline fun <T : Any> LiveData<T>.observe(
  owner: LifecycleOwner,
  crossinline observer: (T) -> Unit,
) = Observer<T?> { it?.let(observer) }
  .also { observe(owner, it) }

inline fun <T : Any> LiveData<Event<T>>.observeEvent(
  owner: LifecycleOwner,
  crossinline observer: (T) -> Unit,
) = Observer { event: Event<T>? ->
  event
    ?.getContentIfNotHandled()
    ?.let(observer)
}.also { observe(owner, it) }

fun <T : Any> LiveData<T>.toObservable(fallbackNullValue: (() -> T)? = null): Observable<T> {
  return Observable.create { emitter: ObservableEmitter<T> ->
    val observer = Observer<T> { value: T? ->
      if (!emitter.isDisposed) {
        val notnullValue = value ?: fallbackNullValue?.invoke() ?: return@Observer
        emitter.onNext(notnullValue)
      }
    }
    observeForever(observer)
    emitter.setCancellable { removeObserver(observer) }
  }.subscribeOn(AndroidSchedulers.mainThread())
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> MutableLiveData<T>.asLiveData(): LiveData<T> = this