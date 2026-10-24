package org.smobba.store

import java.io.File

actual class PlatformReceiptSaver actual constructor() {
    actual fun saveReceipt(orderId: String, textContent: String, onComplete: (Boolean, String) -> Unit) {
        try {
            val userHome = System.getProperty("user.home")
            val downloadsDir = File(userHome, "Downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, "Receipt_$orderId.txt")
            file.writeText(textContent)
            onComplete(true, "บันทึกไฟล์ใบเสร็จที่: ${file.absolutePath}")
        } catch (e: Exception) {
            onComplete(false, "เกิดข้อผิดพลาด: ${e.message}")
        }
    }
}
