package org.smobba.store.ui

import androidx.compose.foundation.background
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
            adminData = api.getAdminData()
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
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.width(360.dp).padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("เข้าสู่ระบบแอดมิน", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyDark)
                    Text("เฉพาะกรรมการสโมสรนักศึกษา BBA UBRU", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("รหัส PIN แอดมิน") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    loginError?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(err, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("เข้าสู่ระบบ", fontWeight = FontWeight.Bold)
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ระบบจัดการหลังบ้าน (Admin Portal)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyDark)
                Text("ตรวจสอบยอดเงิน ดูสลิป และดูใบเสร็จของนักศึกษา", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { refreshData() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = IndigoPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Row
        adminData?.stats?.let { stats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ยอดขายรวม", fontSize = 11.sp, color = Color.Gray)
                        Text("฿${stats.totalSales}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IndigoPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("รอตรวจสอบ", fontSize = 11.sp, color = Color.Gray)
                        Text("${stats.pendingCount} รายการ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFD97706))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("พร้อมรับเสื้อ", fontSize = 11.sp, color = Color.Gray)
                        Text("${stats.readyCount} รายการ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldSuccess)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("รายการคำสั่งจองทั้งหมด (${adminData?.orders?.size ?: 0} รายการ)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoPrimary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(adminData?.orders ?: emptyList()) { order ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
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
                                Column {
                                    Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndigoPrimary)
                                    Text("${order.customerName} (${order.studentId})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("โทร: ${order.phone}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("฿${order.totalAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    Surface(
                                        color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            order.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (order.status.contains("เรียบร้อย") || order.status.contains("พร้อมรับ")) EmeraldSuccess else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(order.itemsSummary, fontSize = 11.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                // View Receipt Button
                                OutlinedButton(
                                    onClick = { onViewReceipt(order) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ดูใบเสร็จ", fontSize = 11.sp)
                                }

                                // Quick Status Action
                                if (order.status.contains("รอตรวจสอบ")) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(order.orderId, "ชำระเงินเรียบร้อย")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Text("อนุมัติยอด", fontSize = 11.sp)
                                    }
                                } else if (order.status == "ชำระเงินเรียบร้อย") {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(order.orderId, "พร้อมรับเสื้อที่ห้องสโมสร")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Text("แจ้งพร้อมรับ", fontSize = 11.sp)
                                    }
                                } else if (order.status == "พร้อมรับเสื้อที่ห้องสโมสร") {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                api.updateOrderStatus(order.orderId, "รับเสื้อแล้ว")
                                                refreshData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("รับเสื้อแล้ว", fontSize = 11.sp)
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
