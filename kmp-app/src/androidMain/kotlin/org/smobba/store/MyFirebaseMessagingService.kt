package org.smobba.store

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM registration token received: $token")
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()

        // Sync with server if user info exists
        val role = prefs.getString(KEY_ROLE, "visitor") ?: "visitor"
        val studentId = prefs.getString(KEY_STUDENT_ID, null)
        val adminPin = prefs.getString(KEY_ADMIN_PIN, null)
        sendTokenToServer(token, role, studentId, adminPin)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "FCM Message received from: ${remoteMessage.from}, data: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "🛒 แจ้งเตือน SMO BBA SHOP"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "มีการอัปเดตคำสั่งซื้อของคุณ"

        val orderId = remoteMessage.data["orderId"]

        showSystemNotification(title, body, orderId)
    }

    private fun showSystemNotification(title: String, message: String, orderId: String?) {
        try {
            ensureNotificationChannel(this)

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (!orderId.isNullOrBlank()) {
                    putExtra("orderId", orderId)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                (System.currentTimeMillis() % 100000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
            }

            val notification = builder
                .setSmallIcon(applicationInfo.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(Notification.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .build()

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
        } catch (e: Exception) {
            Log.e("FCM", "Error displaying notification: ${e.message}", e)
        }
    }

    companion object {
        const val CHANNEL_ID = "bba_orders_channel"
        const val PREFS_NAME = "bba_fcm_prefs"
        const val KEY_FCM_TOKEN = "fcm_token"
        const val KEY_ROLE = "fcm_role"
        const val KEY_STUDENT_ID = "fcm_student_id"
        const val KEY_ADMIN_PIN = "fcm_admin_pin"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "การแจ้งเตือน SMO BBA SHOP",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "แจ้งเตือนคำสั่งซื้อใหม่และอัปเดตสถานะรับเสื้อแบบ Realtime"
                    enableVibration(true)
                    enableLights(true)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        fun sendTokenToServer(
            token: String,
            role: String,
            studentId: String?,
            adminPin: String?
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL("https://bba-store.smobba.workers.dev/api/notifications/register-token")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    conn.doOutput = true
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000

                    val payload = JSONObject().apply {
                        put("token", token)
                        put("role", role)
                        if (!studentId.isNullOrBlank()) put("studentId", studentId)
                        if (!adminPin.isNullOrBlank()) put("pin", adminPin)
                    }

                    OutputStreamWriter(conn.outputStream).use {
                        it.write(payload.toString())
                        it.flush()
                    }

                    val code = conn.responseCode
                    Log.d("FCM", "Register FCM token server response: $code")
                    conn.disconnect()
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to register FCM token with server: ${e.message}")
                }
            }
        }
    }
}
