package org.smobba.store.ui

import androidx.compose.foundation.background
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ใบเสร็จคำสั่งจองเสื้อ BBA UBRU",
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
                        .padding(20.dp)
                ) {
                    // Organization Header
                    Text(
                        text = "สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)",
                        color = IndigoPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ใบเสร็จรับเงิน / ใบยืนยันการสั่งจองเสื้อ (E-Receipt)",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Meta Information
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("รหัสคำสั่งจอง:", fontSize = 12.sp, color = Color.Gray)
                            Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndigoPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("วันที่สั่งจอง: ${order.orderDate}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("รหัสนักศึกษา: ${order.studentId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("ชื่อผู้สั่ง: ${order.customerName}", fontSize = 12.sp)
                            Text("โทร: ${order.phone}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Items Summary
                    Text("รายการสินค้าที่สั่งจอง:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = order.itemsSummary.ifBlank { "เสื้อกิจกรรม BBA 2026 x 1" },
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grand Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ยอดรวมสุทธิ:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "฿${order.totalAmount}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = IndigoPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pickup Details
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📍 สถานที่รับเสื้อ:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                            Text(order.pickupLocation, fontSize = 12.sp, color = IndigoPrimary)
                            Text("วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }

                    saveStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                val textReceipt = buildString {
                                    appendLine("=== ใบเสร็จคำสั่งจองเสื้อ BBA UBRU ===")
                                    appendLine("รหัสคำสั่งจอง: ${order.orderId}")
                                    appendLine("วันที่: ${order.orderDate}")
                                    appendLine("รหัสนักศึกษา: ${order.studentId}")
                                    appendLine("ชื่อ: ${order.customerName}")
                                    appendLine("เบอร์โทร: ${order.phone}")
                                    appendLine("รายการ: ${order.itemsSummary}")
                                    appendLine("ยอดรวมสุทธิ: ฿${order.totalAmount}")
                                    appendLine("สถานที่รับเสื้อ: ${order.pickupLocation}")
                                    appendLine("======================================")
                                }
                                saver.saveReceipt(order.orderId, textReceipt) { success, msg ->
                                    saveStatusMessage = msg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("บันทึกใบเสร็จลงเครื่อง", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Text("ปิด")
                        }
                    }
                }
            }
        }
    }
}
