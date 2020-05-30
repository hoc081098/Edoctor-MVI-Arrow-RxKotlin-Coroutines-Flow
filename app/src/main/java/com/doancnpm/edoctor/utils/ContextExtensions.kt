package com.doancnpm.edoctor.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt

val Context.isOrientationPortrait get() = this.resources.configuration.orientation == ORIENTATION_PORTRAIT

@Suppress("nothing_to_inline")
@ColorInt
inline fun Context.getColorBy(@ColorRes id: Int) = ContextCompat.getColor(this, id)

@Suppress("nothing_to_inline")
inline fun Context.getDrawableBy(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

/**
 * Get uri from any resource type
 * @receiver Context
 * @param resId - Resource id
 * @return - Uri to resource by given id or null
 */
fun Context.uriFromResourceId(@AnyRes resId: Int): Uri? {
  return runCatching {
    val res = this@uriFromResourceId.resources
    Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE +
          "://" + res.getResourcePackageName(resId)
          + '/' + res.getResourceTypeName(resId)
          + '/' + res.getResourceEntryName(resId)
    )
  }.getOrNull()
}

fun Context.dpToPx(dp: Int): Int {
  val displayMetrics = resources.displayMetrics
  return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

@Suppress("nothing_to_inline")
inline fun Context.toast(
  @StringRes messageRes: Int,
  short: Boolean = true,
) = this.toast(getString(messageRes), short)

@Suppress("nothing_to_inline")
inline fun Context.toast(
  message: String,
  short: Boolean = true,
) =
  Toast.makeText(
    this,
    message,
    if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
  ).apply { show() }!!

@SuppressLint("Recycle")
fun Context.themeInterpolator(@AttrRes attr: Int): Interpolator {
  return AnimationUtils.loadInterpolator(
    this,
    obtainStyledAttributes(intArrayOf(attr)).use {
      it.getResourceId(0, android.R.interpolator.fast_out_slow_in)
    }
  )
}

fun Fragment.hideKeyboard() {
  activity?.hideKeyboard(view ?: return)
}

fun AppCompatActivity.hideKeyboard() {
  hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
  val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}