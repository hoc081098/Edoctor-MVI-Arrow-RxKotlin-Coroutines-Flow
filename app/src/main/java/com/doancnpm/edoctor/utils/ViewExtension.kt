@file:Suppress("NOTHING_TO_INLINE") // Aliases to other public API.

package com.doancnpm.edoctor.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

@Suppress("nothing_to_inline")
inline infix fun ViewGroup.inflate(layoutRes: Int) =
  LayoutInflater.from(context).inflate(layoutRes, this, false)!!

inline fun View.gone() {
  isGone = true
}

inline fun View.visible() {
  isVisible = true
}

inline fun View.invisible() {
  isInvisible = true
}