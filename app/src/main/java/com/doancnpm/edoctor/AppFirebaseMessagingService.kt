package com.doancnpm.edoctor

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.doancnpm.edoctor.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL

class AppFirebaseMessagingService : FirebaseMessagingService() {

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  // [START receive_message]
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    // [START_EXCLUDE]
    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    // [END_EXCLUDE]

    // TODO(developer): Handle FCM messages here.
    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    Timber.d("From: ${remoteMessage.from}")

    // Check if message contains a data payload.
    remoteMessage.data.isNotEmpty().let {
      Timber.d("Message data payload: %s", remoteMessage.data)
      GlobalScope.launch { sendNotification(remoteMessage.data.withDefault { "" }) }

      if (/* Check if data needs to be processed by long running job */ true) {
        // For long-running tasks (10 seconds or more) use WorkManager.
        scheduleJob()
      } else {
        // Handle message within 10 seconds
        handleNow()
      }
    }

    // Check if message contains a notification payload.
    remoteMessage.notification?.let {
      Timber.d("Message Notification Body: ${it.body}")
    }

    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.
  }
  // [END receive_message]

  // [START on_new_token]
  /**
   * Called if InstanceID token is updated. This may occur if the security of
   * the previous token had been compromised. Note that this is called when the InstanceID token
   * is initially generated so this is where you would retrieve the token.
   */
  override fun onNewToken(token: String) {
    Timber.d("Refreshed token: $token")

    // If you want to send messages to this application instance or
    // manage this apps subscriptions on the server side, send the
    // Instance ID token to your app server.
    sendRegistrationToServer(token)
  }
  // [END on_new_token]

  /**
   * Schedule async work using WorkManager.
   */
  private fun scheduleJob() {
    // [START dispatch_job]
    // val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
    // WorkManager.getInstance().beginWith(work).enqueue()
    // [END dispatch_job]
  }

  /**
   * Handle time allotted to BroadcastReceivers.
   */
  private fun handleNow() {
    Timber.d("Short lived task is done.")
  }

  /**
   * Persist token to third-party servers.
   *
   * Modify this method to associate the user's FCM InstanceID token with any server-side account
   * maintained by your application.
   *
   * @param token The new token.
   */
  private fun sendRegistrationToServer(token: String?) {
    // TODO: Implement this method to send token to your app server.
    Timber.d("sendRegistrationTokenToServer($token)")
  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   * @param messageBody FCM message body received.
   */
  private suspend fun sendNotification(data: Map<String, String>) {
    val orderId = data.getValue("order_id").toLongOrNull() ?: -1
    val type = data.getValue("type")

    val intent = Intent(this, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
      putExtra(MainActivity.TYPE_KEY, type)
      putExtra(MainActivity.ORDER_ID_KEY, orderId)
    }
    val pendingIntent = PendingIntent.getActivity(
      this,
      orderId.toInt() /* Request code */,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val channelId = getString(R.string.notification_channel_id)
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val notificationBuilder = NotificationCompat
      .Builder(this, channelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setContentTitle(data.getValue("title"))
      .setContentText(data.getValue("body"))
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setShowWhen(true)
      .setContentIntent(pendingIntent)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(
      type,
      orderId.toInt(),
      notificationBuilder.build(),
    )

    val myBitmap = withContext(Dispatchers.IO) {
      try {
        URL(BuildConfig.BASE_URL + data.getValue("image"))
          .openStream()
          .let { BitmapFactory.decodeStream(it) }
      } catch (_: Exception) {
        null
      }
    }

    notificationManager.notify(
      type,
      orderId.toInt(),
      notificationBuilder
        .setStyle(NotificationCompat.BigPictureStyle().bigPicture(myBitmap))
        .build(),
    )
  }
}