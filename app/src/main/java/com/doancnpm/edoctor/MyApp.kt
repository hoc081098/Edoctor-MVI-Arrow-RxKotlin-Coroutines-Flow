package com.doancnpm.edoctor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.doancnpm.edoctor.koin.dataModule
import com.doancnpm.edoctor.koin.domainModule
import com.doancnpm.edoctor.koin.viewModelModule
import com.google.android.libraries.places.api.Places
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@Suppress("unused")
class MyApp : Application() {
  override fun onCreate() {
    super.onCreate()

    setupPlaceApi()
    startKoin()
    setupTimber()
    setupRx()
    createNotificationChannel()
  }

  private fun setupRx() {
    if (!BuildConfig.DEBUG) {
      RxJavaPlugins.setErrorHandler { }
    }
  }

  private fun setupPlaceApi() {
    Places.initialize(this, BuildConfig.PLACE_API_KEY)
  }

  private fun setupTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      // TODO(Timber): plant release tree
    }
  }

  private fun startKoin() {
    startKoin {
      // use AndroidLogger as Koin Logger
      androidLogger(level = if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)

      // use the Android context given there
      androidContext(this@MyApp)

      modules(
        listOf(
          dataModule,
          domainModule,
          viewModelModule,
        )
      )
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        getString(R.string.notification_channel_id),
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply { description = getString(R.string.notification_channel_description) }

      getSystemService(NotificationManager::class.java)!!.createNotificationChannel(channel)
    }
  }
}