package org.smobba.store

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

actual class PlatformReceiptSaver actual constructor() {
    actual fun saveReceipt(orderId: String, textContent: String, onComplete: (Boolean, String) -> Unit) {
        try {
            val fileName = "Receipt_$orderId.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(textContent.toByteArray())
            }
            onComplete(true, "บันทึกใบเสร็จลงในโฟลเดอร์ Downloads/$fileName เรียบร้อยแล้ว")
        } catch (e: Exception) {
            onComplete(false, "เกิดข้อผิดพลาด: ${e.message}")
        }
    }
}
