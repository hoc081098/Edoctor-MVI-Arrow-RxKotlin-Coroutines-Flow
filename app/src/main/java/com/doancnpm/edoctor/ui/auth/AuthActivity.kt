package com.doancnpm.edoctor.ui.auth

import android.os.Bundle
import com.doancnpm.edoctor.core.BaseActivity
import com.doancnpm.edoctor.databinding.ActivityAuthBinding
import kotlin.LazyThreadSafetyMode.NONE

class AuthActivity : BaseActivity() {
  private val binding by lazy(NONE) { ActivityAuthBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
  }
}