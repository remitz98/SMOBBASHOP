package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.smobba.store.PlatformReceiptSaver
import org.smobba.store.models.Order
import org.smobba.store.theme.EmeraldSuccess
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark

@Composable
fun ReceiptDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    val saver = remember { PlatformReceiptSaver() }
    var saveStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Toolbar
                Surface(
                    color = NavyDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ใบเสร็จรับเงินคำสั่งจองเสื้อ BBA UBRU",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Receipt Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Organization Header
                    Text(
                        text = "สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)",
                        color = IndigoPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "ใบเสร็จรับเงิน / ใบยืนยันการสั่งจองเสื้อ (E-Receipt)",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Meta Information
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("รหัสคำสั่งจอง:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IndigoPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("วันที่สั่งจอง: ${order.orderDate}", fontSize = 12.sp, color = Color(0xFF334155))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("รหัสนักศึกษา: ${order.studentId}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                            Text("ชื่อผู้สั่ง: ${order.customerName}", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("โทร: ${order.phone}", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Items Summary
                    Text("รายการสินค้าที่สั่งจอง:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = order.itemsSummary.ifBlank { "เสื้อกิจกรรม BBA 2026 x 1" },
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grand Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ยอดรวมสุทธิ:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                        Text(
                            "฿${order.totalAmount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = IndigoPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pickup Details
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("📍 สถานที่รับเสื้อ:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                order.pickupLocation.ifBlank { "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ" },
                                fontSize = 12.sp,
                                color = NavyDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text("วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    saveStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                msg,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Bottom Buttons
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isSaving = true
                                val textReceipt = buildString {
                                    appendLine("==============================================")
                                    appendLine("      สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ")
                                    appendLine("       มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)")
                                    appendLine("    ใบเสร็จรับเงิน / ใบยืนยันการสั่งจองเสื้อ (E-Receipt)")
                                    appendLine("==============================================")
                                    appendLine("รหัสคำสั่งจอง: ${order.orderId}")
                                    appendLine("วันที่สั่งจอง: ${order.orderDate}")
                                    appendLine("รหัสนักศึกษา:  ${order.studentId}")
                                    appendLine("ชื่อผู้สั่ง:    ${order.customerName}")
                                    appendLine("เบอร์โทรศัพท์: ${order.phone}")
                                    appendLine("อีเมล:        ${order.studentEmail}")
                                    appendLine("----------------------------------------------")
                                    appendLine("รายการสินค้า:")
                                    appendLine(order.itemsSummary)
                                    appendLine("----------------------------------------------")
                                    appendLine("ยอดรวมสุทธิ:   ฿${order.totalAmount.toInt()} บาท")
                                    appendLine("สถานะคำสั่งจอง: ${order.status}")
                                    appendLine("สถานที่รับเสื้อ: ${order.pickupLocation}")
                                    appendLine("เวลาทำการ:    วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.")
                                    appendLine("==============================================")
                                }
                                saver.saveReceipt(order.orderId, textReceipt) { success, msg ->
                                    isSaving = false
                                    saveStatusMessage = msg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("บันทึกใบเสร็จลงเครื่อง", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.35f).height(46.dp)
                        ) {
                            Text("ปิด")
                        }
                    }
                }
            }
        }
    }
}
