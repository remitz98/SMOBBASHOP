package org.smobba.store

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.smobba.store.models.Order
import java.io.File
import java.io.FileOutputStream

actual class PlatformReceiptSaver actual constructor() {
    actual fun saveReceipt(order: Order, onComplete: (Boolean, String) -> Unit) {
        val context = AppContextHolder.context
        val fileName = "Receipt_${order.orderId}.png"

        try {
            val width = 750
            val height = 1000
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. White Background
            canvas.drawColor(Color.WHITE)

            // 2. Navy Header
            paint.color = Color.rgb(30, 27, 75) // #1E1B4B
            canvas.drawRect(0f, 0f, width.toFloat(), 120f, paint)

            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ", 40f, 45f, paint)

            paint.color = Color.rgb(199, 210, 254) // #C7D2FE
            paint.textSize = 17f
            paint.isFakeBoldText = false
            canvas.drawText("มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)", 40f, 75f, paint)

            paint.color = Color.rgb(245, 158, 11) // Gold
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("ใบเสร็จรับเงิน / ใบยืนยันคำสั่งจองเสื้อ (E-Receipt)", 40f, 103f, paint)

            // 3. Receipt Box Border
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 2f
            canvas.drawRoundRect(RectF(30f, 140f, width - 30f, height - 30f), 16f, 16f, paint)
            paint.style = Paint.Style.FILL

            // 4. Order Information
            var y = 175f
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("ข้อมูลคำสั่งจอง", 50f, y, paint)

            y += 28f
            paint.textSize = 15f
            paint.isFakeBoldText = false
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("รหัสคำสั่งจอง:", 50f, y, paint)
            paint.color = Color.rgb(67, 56, 202)
            paint.isFakeBoldText = true
            canvas.drawText(order.orderId, 180f, y, paint)

            y += 24f
            paint.isFakeBoldText = false
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("วันที่สั่งจอง:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(order.orderDate.ifEmpty { "-" }, 180f, y, paint)

            y += 24f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("รหัสนักศึกษา:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(order.studentId.ifEmpty { "-" }, 180f, y, paint)

            y += 24f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("ชื่อผู้สั่งจอง:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(order.customerName.ifEmpty { "-" }, 180f, y, paint)

            y += 24f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("เบอร์โทรศัพท์:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(order.phone.ifEmpty { "-" }, 180f, y, paint)

            y += 24f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("อีเมล:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(order.studentEmail.ifEmpty { "-" }, 180f, y, paint)

            // Divider
            y += 25f
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 1f
            canvas.drawLine(50f, y, width - 50f, y, paint)

            // 5. Items Summary Box
            y += 30f
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("รายการสินค้าที่สั่งจอง", 50f, y, paint)

            y += 26f
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(50f, y - 18f, width - 50f, y + 120f), 12f, 12f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 15f
            paint.isFakeBoldText = false
            val itemLines = order.itemsSummary.split("\n", ";", ",")
            var itemY = y + 10f
            for (line in itemLines.take(5)) {
                if (line.isNotBlank()) {
                    canvas.drawText("• ${line.trim()}", 70f, itemY, paint)
                    itemY += 24f
                }
            }

            // 6. Total Amount Box
            y += 165f
            paint.color = Color.rgb(236, 253, 245)
            canvas.drawRoundRect(RectF(50f, y - 20f, width - 50f, y + 42f), 12f, 12f, paint)

            paint.color = Color.rgb(6, 78, 59)
            paint.textSize = 17f
            paint.isFakeBoldText = true
            canvas.drawText("ยอดรวมสุทธิ:", 70f, y + 16f, paint)

            paint.color = Color.rgb(5, 150, 105)
            paint.textSize = 23f
            canvas.drawText("฿${order.totalAmount.toInt()} บาท", (width - 250).toFloat(), y + 18f, paint)

            // 7. Pickup & Status
            y += 75f
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("ข้อมูลการรับเสื้อและสถานะ", 50f, y, paint)

            y += 24f
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 15f
            paint.isFakeBoldText = false
            canvas.drawText("สถานะคำสั่งจอง:", 50f, y, paint)
            paint.color = Color.rgb(67, 56, 202)
            paint.isFakeBoldText = true
            canvas.drawText(order.status.ifEmpty { "รอตรวจสอบยอดเงิน" }, 180f, y, paint)

            y += 22f
            paint.color = Color.rgb(71, 85, 105)
            paint.isFakeBoldText = false
            canvas.drawText("สถานที่รับเสื้อ:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(order.pickupLocation.ifEmpty { "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ" }, 180f, y, paint)

            y += 22f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("เวลาทำการ:", 50f, y, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.", 180f, y, paint)

            // 8. Footer
            y += 45f
            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 12f
            canvas.drawText("เอกสารนี้ออกโดยระบบอิเล็กทรอนิกส์ สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ", 60f, y, paint)
            y += 18f
            canvas.drawText("กรุณาแสดงภาพใบเสร็จนี้หรือรหัสคำสั่งจองต่อเจ้าหน้าที่เมื่อติดต่อขอรับเสื้อ", 100f, y, paint)

            // Save to MediaStore (Pictures/BBA_Receipts) or external storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context != null) {
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
                    onComplete(true, "✅ บันทึกรูปภาพใบเสร็จ $fileName ลงในแกลเลอรีรูปภาพ (Pictures/BBA_Receipts) เรียบร้อยแล้ว")
                } else {
                    onComplete(false, "ไม่สามารถสร้างไฟล์รูปภาพใน MediaStore ได้")
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BBA_Receipts")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                onComplete(true, "✅ บันทึกรูปภาพใบเสร็จ $fileName ลงใน Pictures/BBA_Receipts เรียบร้อยแล้ว")
            }
        } catch (e: Exception) {
            onComplete(false, "เกิดข้อผิดพลาดในการบันทึกรูปภาพ: ${e.message}")
        }
    }
}
