package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.smobba.store.api.BbaStoreApi
import org.smobba.store.models.Order
import org.smobba.store.theme.EmeraldSuccess
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark

@Composable
fun TrackScreen(
    api: BbaStoreApi,
    onViewReceipt: (Order) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var studentId by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var activeSlipUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Search Form Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📋", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("เช็กสถานะการสั่งจองและรับเสื้อ", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = NavyDark)
                        Text("ตรวจสอบสถานะคำสั่งซื้อและหลักฐานใบเสร็จเฉพาะบัญชีของคุณ", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("รหัสนักศึกษา (12 หรือ 13 หลัก)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("รหัส PIN 4 หลักที่ตั้งไว้ตอนสั่งจอง") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (studentId.isBlank() || pin.isBlank()) return@Button
                        isSearching = true
                        coroutineScope.launch {
                            orders = api.trackOrders(studentId.trim(), pin.trim())
                            isSearching = false
                            searched = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ค้นหาประวัติคำสั่งจอง", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Results
        if (searched && orders.isEmpty() && !isSearching) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ไม่พบประวัติการสั่งจอง หรือรหัส PIN ไม่ถูกต้องสำหรับรหัสนักศึกษานี้",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(orders) { order ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IndigoPrimary)
                            Surface(
                                color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) EmeraldSuccess else Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("วันที่สั่งจอง: ${order.orderDate}", fontSize = 12.sp, color = Color(0xFF64748B))

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Timeline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimelineStep(label = "1. รอตรวจสอบ", isDone = true)
                            TimelineDivider(isDone = order.status != "รอตรวจสอบการชำระเงิน")
                            TimelineStep(label = "2. ชำระเรียบร้อย", isDone = order.status != "รอตรวจสอบการชำระเงิน")
                            TimelineDivider(isDone = order.status.contains("พร้อมรับ") || order.status == "รับเสื้อแล้ว")
                            TimelineStep(label = "3. พร้อมรับเสื้อ", isDone = order.status.contains("พร้อมรับ") || order.status == "รับเสื้อแล้ว")
                            TimelineDivider(isDone = order.status == "รับเสื้อแล้ว")
                            TimelineStep(label = "4. รับเสื้อแล้ว", isDone = order.status == "รับเสื้อแล้ว")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                order.itemsSummary,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ยอดรวมทั้งสิ้น:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("฿${order.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NavyDark)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.slipUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { activeSlipUrl = order.slipUrl },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("ดูสลิปโอนเงิน", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }
                            Button(
                                onClick = { onViewReceipt(order) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = IndigoPrimary),
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ดูใบเสร็จ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        activeSlipUrl?.let { slipUrl ->
            val fullUrl = if (slipUrl.startsWith("http")) slipUrl else "https://bba-store.smobba.workers.dev$slipUrl"
            androidx.compose.ui.window.Dialog(onDismissRequest = { activeSlipUrl = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("สลิปหลักฐานการโอนเงิน", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            coil3.compose.AsyncImage(
                                model = fullUrl,
                                contentDescription = "Slip Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { activeSlipUrl = null },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ปิด")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStep(label: String, isDone: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = if (isDone) EmeraldSuccess else Color(0xFFE2E8F0),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDone) {
                    Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
            color = if (isDone) EmeraldSuccess else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun RowScope.TimelineDivider(isDone: Boolean) {
    HorizontalDivider(
        color = if (isDone) EmeraldSuccess else Color(0xFFE2E8F0),
        thickness = 2.dp,
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp).offset(y = (-8).dp)
    )
}
