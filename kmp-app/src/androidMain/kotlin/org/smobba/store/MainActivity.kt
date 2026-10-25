package org.smobba.store

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST_CODE = 1001
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 2001
    val NOTIFICATION_CHANNEL_ID = "bba_orders_channel"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextHolder.context = applicationContext

        createNotificationChannel()
        checkNotificationPermission()
        initFcm()

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                useWideViewPort = false
                loadWithOverviewMode = false
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "$userAgentString BBAStoreAndroidNativeApp/1.0"
            }

            // Native Android Bridge for Saving Receipts, Notifications & App Controls
            addJavascriptInterface(AndroidNativeBridge(this@MainActivity), "AndroidNative")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("line:")) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            return true
                        } catch (e: Exception) {
                            return false
                        }
                    }
                    return false
                }
            }

            webChromeClient = object : WebChromeClient() {
                // Support file uploads for payment slips and product photos (single & multiple)
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = filePathCallback

                    val intent = try {
                        fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                    } catch (e: Exception) {
                        Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                    }

                    if (fileChooserParams?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }

                    try {
                        startActivityForResult(Intent.createChooser(intent, "เลือกรูปภาพ"), FILE_CHOOSER_REQUEST_CODE)
                    } catch (e: Exception) {
                        fileUploadCallback?.onReceiveValue(null)
                        fileUploadCallback = null
                        return false
                    }
                    return true
                }
            }

            // Direct download listener
            setDownloadListener { url, _, _, _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "ไม่สามารถดาวน์โหลดไฟล์ได้: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            loadUrl("https://bba-store.smobba.workers.dev")
        }

        setContentView(webView)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "การแจ้งเตือน SMO BBA SHOP",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "แจ้งเตือนคำสั่งซื้อใหม่และอัปเดตสถานะคำสั่งจองเสื้อแบบ Realtime"
                enableVibration(true)
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun initFcm() {
        MyFirebaseMessagingService.ensureNotificationChannel(this)
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val prefs = getSharedPreferences(MyFirebaseMessagingService.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putString(MyFirebaseMessagingService.KEY_FCM_TOKEN, token).apply()
                    val role = prefs.getString(MyFirebaseMessagingService.KEY_ROLE, "visitor") ?: "visitor"
                    val studentId = prefs.getString(MyFirebaseMessagingService.KEY_STUDENT_ID, null)
                    val adminPin = prefs.getString(MyFirebaseMessagingService.KEY_ADMIN_PIN, null)
                    MyFirebaseMessagingService.sendTokenToServer(token, role, studentId, adminPin)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            var results: Array<Uri>? = null
            if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val list = ArrayList<Uri>()
                    for (i in 0 until clipData.itemCount) {
                        val itemUri = clipData.getItemAt(i).uri
                        if (itemUri != null) list.add(itemUri)
                    }
                    if (list.isNotEmpty()) {
                        results = list.toTypedArray()
                    }
                } else if (data.data != null) {
                    results = arrayOf(data.data!!)
                }
            }
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    class AndroidNativeBridge(private val activity: MainActivity) {

        @JavascriptInterface
        fun isAndroidNativeApp(): Boolean {
            return true
        }

        @JavascriptInterface
        fun getAppVersionCode(): Int {
            return try {
                val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode
                }
            } catch (e: Exception) {
                25
            }
        }

        @JavascriptInterface
        fun getAppVersionName(): String {
            return try {
                val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                pInfo.versionName ?: "1.0.25"
            } catch (e: Exception) {
                "1.0.25"
            }
        }

        @JavascriptInterface
        fun getAndroidVersion(): Int {
            return Build.VERSION.SDK_INT
        }

        @JavascriptInterface
        fun finishRefresh() {
            // No-op for compatibility
        }

        @JavascriptInterface
        fun syncFcmRole(role: String, studentId: String?, pin: String?) {
            try {
                val prefs = activity.getSharedPreferences(MyFirebaseMessagingService.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(MyFirebaseMessagingService.KEY_ROLE, role)
                    .putString(MyFirebaseMessagingService.KEY_STUDENT_ID, studentId)
                    .putString(MyFirebaseMessagingService.KEY_ADMIN_PIN, pin)
                    .apply()

                val token = prefs.getString(MyFirebaseMessagingService.KEY_FCM_TOKEN, null)
                if (!token.isNullOrBlank()) {
                    MyFirebaseMessagingService.sendTokenToServer(token, role, studentId, pin)
                } else {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val newToken = task.result
                            prefs.edit().putString(MyFirebaseMessagingService.KEY_FCM_TOKEN, newToken).apply()
                            MyFirebaseMessagingService.sendTokenToServer(newToken, role, studentId, pin)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun showOrderNotification(title: String, message: String, orderId: String?) {
            activity.runOnUiThread {
                try {
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        activity,
                        (System.currentTimeMillis() % 100000).toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                    )

                    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        android.app.Notification.Builder(activity, activity.NOTIFICATION_CHANNEL_ID)
                    } else {
                        @Suppress("DEPRECATION")
                        android.app.Notification.Builder(activity)
                    }

                    val notification = builder
                        .setSmallIcon(activity.applicationInfo.icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(android.app.Notification.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDefaults(android.app.Notification.DEFAULT_ALL)
                        .build()

                    val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        @JavascriptInterface
        fun saveBase64Image(base64Data: String, fileName: String) {
            activity.runOnUiThread {
                try {
                    val cleanBase64 = if (base64Data.contains(",")) {
                        base64Data.substringAfter(",")
                    } else base64Data

                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    if (bitmap != null) {
                        val saved = saveBitmapToGallery(activity, bitmap, fileName)
                        if (saved) {
                            Toast.makeText(
                                activity,
                                "✅ บันทึกใบเสร็จ $fileName ลงในแกลเลอรีรูปภาพและโฟลเดอร์เรียบร้อยแล้ว",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(activity, "ไม่สามารถบันทึกไฟล์ภาพได้", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "ไม่สามารถแปลงข้อมูลภาพได้", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(activity, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BBA_Receipts")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                        true
                    } else false
                } else {
                    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BBA_Receipts")
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, fileName)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
