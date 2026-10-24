package org.smobba.store

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

actual class PlatformReceiptSaver actual constructor() {
    actual fun saveReceipt(orderId: String, textContent: String, onComplete: (Boolean, String) -> Unit) {
        val context = AppContextHolder.context
        val fileName = "Receipt_$orderId.txt"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context != null) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(textContent.toByteArray(Charsets.UTF_8))
                    }
                    onComplete(true, "บันทึกใบเสร็จลงในโฟลเดอร์ Downloads/$fileName เรียบร้อยแล้ว")
                } else {
                    onComplete(false, "ไม่สามารถสร้างไฟล์ในโฟลเดอร์ Downloads ได้")
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(textContent.toByteArray(Charsets.UTF_8))
                }
                onComplete(true, "บันทึกใบเสร็จลงในโฟลเดอร์ Downloads/$fileName เรียบร้อยแล้ว")
            }
        } catch (e: Exception) {
            onComplete(false, "เกิดข้อผิดพลาด: ${e.message}")
        }
    }
}
