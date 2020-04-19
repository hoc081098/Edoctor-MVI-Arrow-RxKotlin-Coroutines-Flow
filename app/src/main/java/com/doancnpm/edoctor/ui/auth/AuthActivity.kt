package com.doancnpm.edoctor.ui.auth

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseActivity
import com.doancnpm.edoctor.databinding.ActivityAuthBinding
import com.doancnpm.edoctor.utils.viewBinding

class AuthActivity : BaseActivity() {
  private val binding by viewBinding(ActivityAuthBinding::inflate)
  private lateinit var appBarConfiguration: AppBarConfiguration

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    appBarConfiguration = AppBarConfiguration(navController.graph)
    setupActionBarWithNavController(
      navController,
      appBarConfiguration
    )
  }

  override fun onSupportNavigateUp(): Boolean =
    findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}