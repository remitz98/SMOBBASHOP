package org.smobba.store

import org.smobba.store.models.Order
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

actual class PlatformReceiptSaver actual constructor() {
    actual fun saveReceipt(order: Order, onComplete: (Boolean, String) -> Unit) {
        try {
            val width = 750
            val height = 1000
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val g: Graphics2D = image.createGraphics()

            // Enable high quality rendering hints
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            // 1. Background
            g.color = Color.WHITE
            g.fillRect(0, 0, width, height)

            // 2. Header banner (Navy)
            g.color = Color(30, 27, 75) // #1E1B4B
            g.fillRect(0, 0, width, 120)

            // Header Texts
            g.color = Color.WHITE
            g.font = Font("SansSerif", Font.BOLD, 22)
            g.drawString("สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ", 40, 45)

            g.color = Color(199, 210, 254) // #C7D2FE
            g.font = Font("SansSerif", Font.PLAIN, 16)
            g.drawString("มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)", 40, 75)

            g.color = Color(245, 158, 11) // Amber gold
            g.font = Font("SansSerif", Font.BOLD, 17)
            g.drawString("ใบเสร็จรับเงิน / ใบยืนยันคำสั่งจองเสื้อ (E-Receipt)", 40, 103)

            // 3. Receipt Box border
            g.color = Color(226, 232, 240) // #E2E8F0
            g.drawRoundRect(30, 140, width - 60, height - 170, 16, 16)

            // 4. Order Information
            var y = 175
            g.color = Color(15, 23, 42) // Slate 900
            g.font = Font("SansSerif", Font.BOLD, 16)
            g.drawString("ข้อมูลคำสั่งจอง", 50, y)

            y += 28
            g.font = Font("SansSerif", Font.PLAIN, 15)
            g.color = Color(71, 85, 105) // Slate 600
            g.drawString("รหัสคำสั่งจอง:", 50, y)
            g.color = Color(67, 56, 202) // Indigo
            g.font = Font("SansSerif", Font.BOLD, 15)
            g.drawString(order.orderId, 180, y)

            y += 24
            g.color = Color(71, 85, 105)
            g.font = Font("SansSerif", Font.PLAIN, 14)
            g.drawString("วันที่สั่งจอง:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString(order.orderDate.ifEmpty { "-" }, 180, y)

            y += 24
            g.color = Color(71, 85, 105)
            g.drawString("รหัสนักศึกษา:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString(order.studentId.ifEmpty { "-" }, 180, y)

            y += 24
            g.color = Color(71, 85, 105)
            g.drawString("ชื่อผู้สั่งจอง:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString(order.customerName.ifEmpty { "-" }, 180, y)

            y += 24
            g.color = Color(71, 85, 105)
            g.drawString("เบอร์โทรศัพท์:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString(order.phone.ifEmpty { "-" }, 180, y)

            y += 24
            g.color = Color(71, 85, 105)
            g.drawString("อีเมล:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString(order.studentEmail.ifEmpty { "-" }, 180, y)

            // Divider
            y += 25
            g.color = Color(226, 232, 240)
            g.drawLine(50, y, width - 50, y)

            // 5. Items Summary Box
            y += 30
            g.color = Color(15, 23, 42)
            g.font = Font("SansSerif", Font.BOLD, 16)
            g.drawString("รายการสินค้าที่สั่งจอง", 50, y)

            y += 26
            g.color = Color(248, 250, 252)
            g.fillRoundRect(50, y - 18, width - 100, 140, 12, 12)
            g.color = Color(203, 213, 225)
            g.drawRoundRect(50, y - 18, width - 100, 140, 12, 12)

            g.color = Color(30, 41, 59)
            g.font = Font("SansSerif", Font.PLAIN, 14)
            val itemLines = order.itemsSummary.split("\n", ";", ",")
            var itemY = y + 10
            for (line in itemLines.take(5)) {
                if (line.isNotBlank()) {
                    g.drawString("• ${line.trim()}", 70, itemY)
                    itemY += 22
                }
            }

            // 6. Total Amount Box
            y += 165
            g.color = Color(236, 253, 245) // Emerald 50
            g.fillRoundRect(50, y - 20, width - 100, 60, 12, 12)
            g.color = Color(16, 185, 129) // Emerald 500
            g.drawRoundRect(50, y - 20, width - 100, 60, 12, 12)

            g.color = Color(6, 78, 59)
            g.font = Font("SansSerif", Font.BOLD, 16)
            g.drawString("ยอดรวมสุทธิ:", 70, y + 16)

            g.color = Color(5, 150, 105)
            g.font = Font("SansSerif", Font.BOLD, 22)
            g.drawString("฿${order.totalAmount.toInt()} บาท", width - 260, y + 18)

            // 7. Pickup & Status
            y += 75
            g.color = Color(15, 23, 42)
            g.font = Font("SansSerif", Font.BOLD, 15)
            g.drawString("ข้อมูลการรับเสื้อและสถานะ", 50, y)

            y += 24
            g.color = Color(71, 85, 105)
            g.font = Font("SansSerif", Font.PLAIN, 14)
            g.drawString("สถานะคำสั่งจอง:", 50, y)
            g.color = Color(67, 56, 202)
            g.font = Font("SansSerif", Font.BOLD, 14)
            g.drawString(order.status.ifEmpty { "รอตรวจสอบยอดเงิน" }, 180, y)

            y += 22
            g.color = Color(71, 85, 105)
            g.font = Font("SansSerif", Font.PLAIN, 14)
            g.drawString("สถานที่รับเสื้อ:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString(order.pickupLocation.ifEmpty { "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ" }, 180, y)

            y += 22
            g.color = Color(71, 85, 105)
            g.drawString("เวลาทำการ:", 50, y)
            g.color = Color(15, 23, 42)
            g.drawString("วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.", 180, y)

            // 8. Official Seal / Footer
            y += 45
            g.color = Color(148, 163, 184)
            g.font = Font("SansSerif", Font.PLAIN, 12)
            g.drawString("เอกสารนี้ออกโดยระบบอิเล็กทรอนิกส์ สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ", 70, y)
            y += 18
            g.drawString("กรุณาแสดงภาพใบเสร็จนี้หรือรหัสคำสั่งจองต่อเจ้าหน้าที่เมื่อติดต่อขอรับเสื้อ", 110, y)

            g.dispose()

            // Save to Downloads folder as PNG
            val userHome = System.getProperty("user.home")
            val downloadsDir = File(userHome, "Downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, "Receipt_${order.orderId}.png")
            ImageIO.write(image, "PNG", file)

            onComplete(true, "✅ บันทึกรูปภาพใบเสร็จเรียบร้อยแล้วที่:\n${file.absolutePath}")
        } catch (e: Exception) {
            onComplete(false, "เกิดข้อผิดพลาดในการบันทึกรูปภาพ: ${e.message}")
        }
    }
}
