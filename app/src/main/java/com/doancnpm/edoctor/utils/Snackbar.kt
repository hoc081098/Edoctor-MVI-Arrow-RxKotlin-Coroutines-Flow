package com.doancnpm.edoctor.utils

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

inline fun View.snack(
  @StringRes messageRes: Int,
  length: SnackbarLength = SnackbarLength.SHORT,
  crossinline f: Snackbar.() -> Unit = {},
) = snack(resources.getString(messageRes), length, f)

inline fun View.snack(
  message: String,
  length: SnackbarLength = SnackbarLength.SHORT,
  crossinline f: Snackbar.() -> Unit = {},
) = Snackbar.make(this, message, length.rawValue).apply {
  f()
  show()
}

fun Snackbar.action(
  @StringRes actionRes: Int,
  color: Int? = null,
  listener: (View) -> Unit,
) = action(view.resources.getString(actionRes), color, listener)

fun Snackbar.action(
  action: String,
  color: Int? = null,
  listener: (View) -> Unit,
) = apply {
  setAction(action, listener)
  color?.let { setActionTextColor(color) }
}

fun Snackbar.onDismissed(onDismissed: (SnackbarDismissEvent) -> Unit): Snackbar.Callback {
  return object : Snackbar.Callback() {
    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
      onDismissed(SnackbarDismissEvent(event))
      removeCallback(this)
    }
  }.also { addCallback(it) }
}

enum class SnackbarDismissEvent(val rawValue: Int) {
  /** Indicates that the Snackbar was dismissed via a swipe.  */
  DISMISS_EVENT_SWIPE(Snackbar.Callback.DISMISS_EVENT_SWIPE),

  /** Indicates that the Snackbar was dismissed via an action click.  */
  DISMISS_EVENT_ACTION(Snackbar.Callback.DISMISS_EVENT_ACTION),

  /** Indicates that the Snackbar was dismissed via a timeout.  */
  DISMISS_EVENT_TIMEOUT(Snackbar.Callback.DISMISS_EVENT_TIMEOUT),

  /** Indicates that the Snackbar was dismissed via a call to [.dismiss].  */
  DISMISS_EVENT_MANUAL(Snackbar.Callback.DISMISS_EVENT_MANUAL),

  /** Indicates that the Snackbar was dismissed from a new Snackbar being shown.  */
  DISMISS_EVENT_CONSECUTIVE(Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE);

  companion object Factory {
    operator fun invoke(rawValue: Int): SnackbarDismissEvent =
      values().first { it.rawValue == rawValue }
  }
}

enum class SnackbarLength(val rawValue: Int) {
  SHORT(Snackbar.LENGTH_SHORT),

  LONG(Snackbar.LENGTH_LONG),

  INDEFINITE(Snackbar.LENGTH_INDEFINITE);
}