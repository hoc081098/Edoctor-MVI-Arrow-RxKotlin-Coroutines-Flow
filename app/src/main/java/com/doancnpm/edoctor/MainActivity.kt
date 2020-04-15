package com.doancnpm.edoctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.doancnpm.edoctor.databinding.ActivityMainBinding
import com.doancnpm.edoctor.utils.setupWithNavController
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : AppCompatActivity() {

  private var currentNavController: LiveData<NavController>? = null

  private val binding by lazy(NONE) {
    ActivityMainBinding.inflate(layoutInflater)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    if (savedInstanceState === null) {
      setupBottomNavigationBar()
    }
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
      R.navigation.dashboard,
      R.navigation.notifications,
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
