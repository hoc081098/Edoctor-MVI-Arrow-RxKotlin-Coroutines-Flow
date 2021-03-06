package com.doancnpm.edoctor.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseActivity
import com.doancnpm.edoctor.databinding.ActivityMainBinding
import com.doancnpm.edoctor.ui.auth.AuthActivity
import com.doancnpm.edoctor.ui.main.history.HistoryContract
import com.doancnpm.edoctor.utils.*
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : BaseActivity() {

  private var currentNavController: LiveData<NavController>? = null
  private val binding by viewBinding(ActivityMainBinding::inflate)
  private val viewModel by viewModel<MainVM>()

  var onSupportNavigateUp: (() -> Boolean)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    if (savedInstanceState === null) {
      setupBottomNavigationBar()
    } else {
      dismissAlertDialog()
    }

    bindVM()
    handleIntent(intent)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    intent ?: return

    val type = intent.getStringExtra(TYPE_KEY) ?: return
    val orderId = intent.getSerializableExtra(ORDER_ID_KEY) as? Long ?: return
    Timber.d(">> handleIntent { orderId: $orderId, type: $type }")

    navigateToHistory(
      type = when (type) {
        "Accepted mission" -> HistoryContract.HistoryType.UP_COMING
        "Check QR Code" -> HistoryContract.HistoryType.PROCESSING
        "Mission completed" -> HistoryContract.HistoryType.DONE
        else -> HistoryContract.HistoryType.WAITING.also {
          Timber.d("Invalid notification type: $type")
        }
      },
      orderId = orderId,
    )
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
    onSupportNavigateUp?.invoke()?.let { return it }
    return currentNavController?.value?.navigateUp() ?: false
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    Timber.d("[onActivityResult] { requestCode: $requestCode, resultCode: $resultCode, data: $data }")

    (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return)
      .childFragmentManager
      .fragments
      .forEach { it.onActivityResult(requestCode, resultCode, data) }
  }

  fun navigateToHistory(type: HistoryContract.HistoryType, orderId: Long) {
    viewModel.setHistoryType(type)
    viewModel.setOrderId(orderId)
    binding.navView.selectedItemId = R.id.history
  }

  fun hideBottomNav() = binding.navView.gone()

  fun showBottomNav() = binding.navView.visible()

  companion object {
    val TYPE_KEY = MainActivity::class.java.name + ".type"
    val ORDER_ID_KEY = MainActivity::class.java.name + ".order_id"
  }
}
