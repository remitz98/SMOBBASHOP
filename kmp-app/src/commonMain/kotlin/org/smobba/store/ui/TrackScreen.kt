package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Search Form Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("เช็กสถานะคำสั่งจองเสื้อ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                Text("กรอกรหัสนักศึกษาและรหัส PIN 4 หลักที่ตั้งไว้ตอนสั่งจอง", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("รหัสนักศึกษา") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("รหัส PIN 4 หลัก") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ค้นหาคำสั่งจอง")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        if (searched && orders.isEmpty() && !isSearching) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("ไม่พบประวัติการสั่งจอง หรือรหัส PIN ไม่ถูกต้อง", color = Color.Gray, fontSize = 13.sp)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(orders) { order ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndigoPrimary)
                            Surface(
                                color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) EmeraldSuccess else Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("วันที่: ${order.orderDate}", fontSize = 11.sp, color = Color.Gray)
                        Text(order.itemsSummary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Text("ยอดรวม: ฿${order.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onViewReceipt(order) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = IndigoPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ดูใบเสร็จรับเงิน (E-Receipt)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
