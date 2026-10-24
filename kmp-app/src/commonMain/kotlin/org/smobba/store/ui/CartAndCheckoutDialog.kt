package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.smobba.store.api.BbaStoreApi
import org.smobba.store.models.CartItem
import org.smobba.store.models.CreateOrderRequest
import org.smobba.store.models.Order
import org.smobba.store.models.OrderItem
import org.smobba.store.theme.EmeraldSuccess
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark

@Composable
fun CartAndCheckoutDialog(
    cart: List<CartItem>,
    api: BbaStoreApi,
    onCartChange: (List<CartItem>) -> Unit,
    onOrderSuccess: (Order) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var customerName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var studentPin by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalAmount = remember(cart) { cart.sumOf { it.subtotal } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(color = NavyDark, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ตะกร้าสินค้า & ยืนยันการสั่งจองเสื้อ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text("รายการเสื้อในตะกร้า (${cart.size} รายการ)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (cart.isEmpty()) {
                        Text("ยังไม่มีสินค้าในตะกร้า", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))
                    } else {
                        cart.forEachIndexed { index, item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                        Text("ไซส์: ${item.selectedSize} | สี: ${item.selectedColor}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("฿${item.unitPrice.toInt()} x ${item.qty} ตัว = ฿${item.subtotal.toInt()}", fontSize = 12.sp, color = IndigoPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = {
                                        val updated = cart.toMutableList().also { it.removeAt(index) }
                                        onCartChange(updated)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE11D48))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("ข้อมูลผู้สั่งจองเสื้อ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("ชื่อ-นามสกุล ผู้สั่งจอง") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("รหัสนักศึกษา (12 หรือ 13 หลัก)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("อีเมลสำหรับรับใบเสร็จ (เว้นว่างได้)") },
                        placeholder = { Text("std.<รหัส>@ubru.ac.th") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("เบอร์โทรศัพท์ติดต่อรับเสื้อ (10 หลัก)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = studentPin,
                        onValueChange = { studentPin = it },
                        label = { Text("กำหนดรหัส PIN 4 หลัก (สำหรับเช็กสถานะคำสั่งจอง)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ช่องทางการชำระเงิน: พร้อมเพย์ (PromptPay)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndigoPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("หมายเลขพร้อมเพย์: 004999245434427", fontSize = 12.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                            Text("ชื่อบัญชี: สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ", fontSize = 12.sp, color = Color(0xFF475569))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ยอดชำระทั้งสิ้น:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("฿${totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = EmeraldSuccess)
                            }
                        }
                    }

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(err, color = Color(0xFFE11D48), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Footer Buttons
                Surface(color = Color.White, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (customerName.isBlank() || studentId.isBlank() || phone.isBlank() || studentPin.isBlank()) {
                                    errorMessage = "กรุณากรอกข้อมูลผู้สั่งจองให้ครบถ้วนทุกช่อง"
                                    return@Button
                                }
                                if (studentPin.trim().length != 4) {
                                    errorMessage = "รหัส PIN ต้องมี 4 หลักเท่านั้น"
                                    return@Button
                                }
                                if (cart.isEmpty()) {
                                    errorMessage = "ไม่มีสินค้าในตะกร้า"
                                    return@Button
                                }

                                isSubmitting = true
                                errorMessage = null

                                coroutineScope.launch {
                                    val orderItems = cart.map {
                                        OrderItem(
                                            productId = it.product.productId,
                                            name = it.product.name,
                                            size = it.selectedSize,
                                            color = it.selectedColor,
                                            qty = it.qty,
                                            price = it.unitPrice,
                                            subtotal = it.subtotal
                                        )
                                    }

                                    val finalEmail = if (email.isNotBlank()) email.trim() else "std.${studentId.trim()}@ubru.ac.th"

                                    val req = CreateOrderRequest(
                                        customerName = customerName.trim(),
                                        studentId = studentId.trim(),
                                        studentEmail = finalEmail,
                                        phone = phone.trim(),
                                        studentPin = studentPin.trim(),
                                        items = orderItems
                                    )

                                    val res = api.createOrder(req)
                                    isSubmitting = false

                                    if (res.success) {
                                        val newOrder = Order(
                                            orderId = res.orderId,
                                            orderDate = "วันนี้",
                                            studentId = studentId.trim(),
                                            customerName = customerName.trim(),
                                            studentEmail = finalEmail,
                                            phone = phone.trim(),
                                            itemsSummary = orderItems.joinToString("\n") { "${it.name} [ไซส์ ${it.size}] x ${it.qty}" },
                                            totalAmount = if (res.totalAmount > 0) res.totalAmount else totalAmount,
                                            status = "รอตรวจสอบการชำระเงิน"
                                        )
                                        onCartChange(emptyList())
                                        onOrderSuccess(newOrder)
                                    } else {
                                        errorMessage = res.message.ifBlank { "การสั่งซื้อไม่สำเร็จ โปรดลองใหม่อีกครั้ง" }
                                    }
                                }
                            },
                            enabled = !isSubmitting && cart.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("ยืนยันการสั่งจองเสื้อ (฿${totalAmount.toInt()})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.35f).height(46.dp)
                        ) {
                            Text("ยกเลิก")
                        }
                    }
                }
            }
        }
    }
}
