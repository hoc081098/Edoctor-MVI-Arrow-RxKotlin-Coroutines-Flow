package com.doancnpm.edoctor

import android.os.Bundle
import com.doancnpm.edoctor.core.BaseActivity
import com.doancnpm.edoctor.databinding.ActivityLoginBinding
import kotlin.LazyThreadSafetyMode.NONE

class LoginActivity : BaseActivity() {

  private val binding by lazy(NONE) { ActivityLoginBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
  }
}