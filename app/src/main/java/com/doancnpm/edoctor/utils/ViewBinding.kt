package com.doancnpm.edoctor.utils

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.android.MainThreadDisposable.verifyMainThread
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import timber.log.Timber
import java.lang.reflect.Method
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T : ViewBinding>(
  private val fragment: Fragment,
  private val viewBindingFactory: (View) -> T,
  private var onDestroy: ((T) -> Unit)?,
) : ReadOnlyProperty<Fragment, T> {
  private var binding: T? = null

  init {
    fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
      override fun onCreate(owner: LifecycleOwner) {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
          Timber.d("$fragment::view::viewLifecycleOwnerLiveData")

          viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
              onDestroy?.invoke(binding!!)
              binding = null
              viewLifecycleOwner.lifecycle.removeObserver(this)
              Timber.d("$fragment::view::onDestroy")
            }
          })
        }
      }

      override fun onDestroy(owner: LifecycleOwner) {
        fragment.lifecycle.removeObserver(this)
      }
    })
  }

  override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
    binding?.let { return it }

    if (!fragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
      error("Attempt to get view binding when fragment view is destroyed")
    }

    return viewBindingFactory(thisRef.requireView()).also { binding = it }
  }
}

@MainThread
inline fun <reified T : ViewBinding> Fragment.viewBinding(noinline onDestroy: (T.() -> Unit)? = null): ViewBindingDelegate<T> {
  var bindMethod: Method? = null

  return ViewBindingDelegate(
    this,
    {
      (bindMethod ?: T::class.java
        .getMethod("bind", View::class.java)
        .also { bindMethod = it })
        .invoke(null, it) as T
    },
    onDestroy
  )
}

fun <T : ViewBinding> AppCompatActivity.viewBinding(factory: (LayoutInflater) -> T) =
  lazy(NONE) { factory(layoutInflater) }

@CheckResult
fun ChipGroup.checkedIds(): InitialValueObservable<Int> = ChipGroupCheckedIdObservable(this)

private class ChipGroupCheckedIdObservable(private val view: ChipGroup) : InitialValueObservable<Int>() {
  override val initialValue: Int
    get() = view.checkedChipId

  override fun subscribeListener(observer: Observer<in Int>) {
    verifyMainThread()

    val listener = Listener(view, observer)
    observer.onSubscribe(listener)
    view.setOnCheckedChangeListener(listener)
  }

  private class Listener(
    private val view: ChipGroup,
    private val observer: Observer<in Int>
  ) : ChipGroup.OnCheckedChangeListener, MainThreadDisposable() {
    override fun onCheckedChanged(group: ChipGroup?, checkedId: Int) {
      if (!isDisposed) {
        observer.onNext(checkedId)
      }
    }

    override fun onDispose() = view.setOnCheckedChangeListener(null)
  }
}

@CheckResult
fun TabLayout.selectedTabPositions(): Observable<Int> {
  return Observable.create { emitter ->
    verifyMainThread()

    val listener = object : TabLayout.OnTabSelectedListener {
      override fun onTabReselected(tab: TabLayout.Tab?) {
      }

      override fun onTabUnselected(tab: TabLayout.Tab?) {
      }

      override fun onTabSelected(tab: TabLayout.Tab?) {
        emitter.onNext(tab?.position ?: return)
      }
    }
    addOnTabSelectedListener(listener)
    emitter.setDisposable(object : MainThreadDisposable() {
      override fun onDispose() = removeOnTabSelectedListener(listener)
    })
  }
}

fun EditText.firstChange(): Observable<Unit> {
  return textChanges()
    .skipInitialValue()
    .unsubscribeOn(AndroidSchedulers.mainThread())
    .take(1)
    .map { Unit }
}

fun TextInputLayout.editTextFirstChange() = editText!!.firstChange()