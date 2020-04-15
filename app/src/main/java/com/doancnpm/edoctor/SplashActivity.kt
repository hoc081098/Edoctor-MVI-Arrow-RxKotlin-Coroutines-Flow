package com.doancnpm.edoctor

import android.content.Intent
import android.os.Bundle
import com.doancnpm.edoctor.core.BaseActivity
import com.doancnpm.edoctor.utils.observeEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {
  private val vm by viewModel<SplashVM>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    vm.authEvent.observeEvent(this) {
      if (it) {
        startActivity(Intent(this, MainActivity::class.java))
      } else {
        startActivity(Intent(this, LoginActivity::class.java))
      }
      finish()
    }
  }
}