package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
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
import org.smobba.store.models.AdminDataResponse
import org.smobba.store.models.Order
import org.smobba.store.theme.EmeraldSuccess
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark

@Composable
fun AdminScreen(
    api: BbaStoreApi,
    onViewReceipt: (Order) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoggedIn by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    var adminData by remember { mutableStateOf<AdminDataResponse?>(null) }
    var isLoadingData by remember { mutableStateOf(false) }

    fun refreshData() {
        isLoadingData = true
        coroutineScope.launch {
            adminData = api.getAdminData(pin.trim())
            isLoadingData = false
        }
    }

    if (!isLoggedIn) {
        // PIN Login
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.width(380.dp).padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("เข้าสู่ระบบแอดมิน", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyDark)
                    Text("เฉพาะกรรมการสโมสรนักศึกษา BBA UBRU", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("รหัส PIN แอดมิน") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    loginError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(err, color = Color(0xFFE11D48), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            if (pin.isBlank()) return@Button
                            isLoggingIn = true
                            loginError = null
                            coroutineScope.launch {
                                val ok = api.adminLogin(pin.trim())
                                isLoggingIn = false
                                if (ok) {
                                    isLoggedIn = true
                                    refreshData()
                                } else {
                                    loginError = "รหัส PIN ไม่ถูกต้อง"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("เข้าสู่ระบบ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        return
    }

    // Admin Dashboard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ระบบจัดการหลังบ้าน (Admin Portal)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyDark)
                Text("ตรวจสอบยอดเงิน ยืนยันสถานะ และดูใบเสร็จของนักศึกษา", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            IconButton(
                onClick = { refreshData() },
                modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = IndigoPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        adminData?.stats?.let { stats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ยอดขายรวม", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("฿${stats.totalSales.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = IndigoPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("รอตรวจสอบ", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.pendingCount} รายการ", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFD97706))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("พร้อมรับเสื้อ", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.readyCount} รายการ", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = EmeraldSuccess)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("รับเสื้อแล้ว", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.completedCount} รายการ", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NavyDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "รายการคำสั่งจองทั้งหมด (${adminData?.orders?.size ?: 0} รายการ)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NavyDark
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoPrimary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(adminData?.orders ?: emptyList()) { order ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndigoPrimary)
                                    Text("${order.customerName} (${order.studentId})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("โทร: ${order.phone} | วันที่: ${order.orderDate}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("฿${order.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            order.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) EmeraldSuccess else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    order.itemsSummary,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                // View Receipt Button
                                OutlinedButton(
                                    onClick = { onViewReceipt(order) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ดูใบเสร็จ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // Quick Status Action
                                if (order.status.contains("รอตรวจสอบ")) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(pin.trim(), order.orderId, "ชำระเงินเรียบร้อย")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                                    ) {
                                        Text("อนุมัติยอด", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (order.status == "ชำระเงินเรียบร้อย") {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(pin.trim(), order.orderId, "พร้อมรับเสื้อที่ห้องสโมสร")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                                    ) {
                                        Text("แจ้งพร้อมรับ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (order.status == "พร้อมรับเสื้อที่ห้องสโมสร") {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(pin.trim(), order.orderId, "รับเสื้อแล้ว")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("รับเสื้อแล้ว", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (order.status == "รับเสื้อแล้ว") {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(pin.trim(), order.orderId, "พร้อมรับเสื้อที่ห้องสโมสร")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ย้อนสถานะรับเสื้อ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
