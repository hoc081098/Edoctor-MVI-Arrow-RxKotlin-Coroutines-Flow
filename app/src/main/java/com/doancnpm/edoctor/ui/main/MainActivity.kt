package com.doancnpm.edoctor.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseActivity
import com.doancnpm.edoctor.databinding.ActivityMainBinding
import com.doancnpm.edoctor.ui.auth.AuthActivity
import com.doancnpm.edoctor.utils.setupWithNavController
import com.doancnpm.edoctor.utils.viewBinding
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : BaseActivity() {

  private var currentNavController: LiveData<NavController>? = null
  private val binding by viewBinding(ActivityMainBinding::inflate)
  private val viewModel by viewModel<MainVM>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    if (savedInstanceState === null) {
      setupBottomNavigationBar()
    }

    bindVM()
  }

  private fun bindVM() {
    viewModel
      .logoutEvent
      .subscribeBy {
        Timber.d("[LOGOUT]")
        startActivity(Intent(this@MainActivity, AuthActivity::class.java))
        finish()
      }
      .addTo(compositeDisposable)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    // Now that BottomNavigationBar has restored its instance state
    // and its selectedItemId, we can proceed with setting up the
    // BottomNavigationBar with Navigation
    setupBottomNavigationBar()
  }

  /**
   * Called on first creation and when restoring state.
   */
  private fun setupBottomNavigationBar() {
    val navGraphIds = listOf(
      R.navigation.home,
      R.navigation.history,
      R.navigation.notification,
      R.navigation.profile,
    )

    // Setup the bottom navigation view with a list of navigation graphs
    val controller = binding.navView.setupWithNavController(
      navGraphIds = navGraphIds,
      fragmentManager = supportFragmentManager,
      containerId = R.id.nav_host_fragment,
      intent = intent
    )

    // Whenever the selected controller changes, setup the action bar.
    controller.observe(this, Observer { navController ->
      setupActionBarWithNavController(navController)
    })
    currentNavController = controller
  }

  override fun onSupportNavigateUp(): Boolean {
    return currentNavController?.value?.navigateUp() ?: false
  }
}
