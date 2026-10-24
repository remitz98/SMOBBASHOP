package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import org.smobba.store.models.CartItem
import org.smobba.store.models.Product
import org.smobba.store.models.StoreResponse
import org.smobba.store.theme.EmeraldSuccess
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark

@Composable
fun StoreScreen(
    storeData: StoreResponse?,
    products: List<Product>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    cart: List<CartItem>,
    onAddToCart: (CartItem) -> Unit,
    onOpenCart: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }
    var viewingProduct by remember { mutableStateOf<Product?>(null) }
    var showSizeGuide by remember { mutableStateOf(false) }

    val categories = remember(products, storeData) {
        val cats = mutableListOf("ทั้งหมด")
        if (storeData != null && storeData.categories.isNotEmpty()) {
            cats.addAll(storeData.categories.filter { it.isNotBlank() })
        } else {
            cats.addAll(products.map { it.category }.filter { it.isNotBlank() }.distinct())
        }
        cats.distinct()
    }

    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "ทั้งหมด") products else products.filter { it.category == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Hero Banner Card with Gradient
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(26.dp)
                ) {
                    Column {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "🏛️ สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มหาวิทยาลัยราชภัฏอุบลราชธานี",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "เปิดจองเสื้อสโมสรนักศึกษา BBA UBRU",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "สั่งจองและชำระเงินออนไลน์ จากนั้นนำหลักฐานใบเสร็จมารับเสื้อได้ด้วยตนเองที่ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pickup Details Card
                        Surface(
                            color = Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📍", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        storeData?.clubLocation?.ifBlank { "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ" } ?: "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🕒", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        storeData?.clubSchedule?.ifBlank { "วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น." } ?: "วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Toolbar: Categories Filter Pills & Size Guide Button
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            color = if (isSelected) IndigoPrimary else Color.White,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) IndigoPrimary else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Size Guide Button
                OutlinedButton(
                    onClick = { showSizeGuide = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(IndigoPrimary, IndigoPrimary))),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ตารางเทียบไซส์เสื้อ", fontSize = 12.sp, color = IndigoPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Products Grid / States
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = IndigoPrimary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "กำลังเชื่อมต่อและโหลดรายการเสื้อจากสโมสรนักศึกษา...",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.padding(20.dp).fillMaxWidth(0.85f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "เกิดข้อผิดพลาดในการโหลดข้อมูล",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFE11D48)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                errorMessage,
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ลองใหม่อีกครั้ง", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ไม่พบรายการเสื้อในหมวดหมู่นี้", color = Color(0xFF64748B), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onRetry,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("รีเฟรชข้อมูล")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 270.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            onSelect = { viewingProduct = product }
                        )
                    }
                }
            }
        }

        // Floating Cart Button
        if (cart.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = onOpenCart,
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                text = { Text("ดูตะกร้าสินค้า (${cart.sumOf { it.qty }} ตัว - ฿${cart.sumOf { it.subtotal }.toInt()})", fontWeight = FontWeight.Bold) },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(28.dp)
            )
        }
    }

    // Detail Dialog
    viewingProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { viewingProduct = null },
            onOpenSizeGuide = { showSizeGuide = true },
            onAdd = { item ->
                onAddToCart(item)
                viewingProduct = null
            }
        )
    }

    // Size Guide Dialog
    if (showSizeGuide) {
        SizeGuideDialog(onDismiss = { showSizeGuide = false })
    }
}

@Composable
fun ProductCard(
    product: Product,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Shirt Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                } else {
                    Text("ไม่มีรูปภาพ", color = Color.Gray)
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        product.category.ifBlank { "เสื้อกิจกรรม BBA" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (product.stock > 0) {
                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            "พร้อมจำหน่าย • คงเหลือ ${product.stock} ตัว",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    lineHeight = 22.sp,
                    color = NavyDark
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "เนื้อผ้านุ่ม ระบายอากาศดีเยี่ยม ใส่สบาย ไม่หด ไม่ย้วย",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ราคาเริ่มต้น", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "฿${product.price.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = IndigoPrimary
                        )
                    }

                    Button(
                        onClick = onSelect,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("สั่งจองเสื้อ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onOpenSizeGuide: () -> Unit,
    onAdd: (CartItem) -> Unit
) {
    val availableSizes = remember(product) {
        product.sizes.ifEmpty { listOf("SS", "S", "M", "L", "XL", "2XL", "3XL", "4XL") }
    }
    val availableColors = remember(product) {
        product.colors.ifEmpty { listOf("สีกรมท่า") }
    }

    var selectedSize by remember { mutableStateOf(availableSizes.firstOrNull() ?: "L") }
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "สีกรมท่า") }
    var qty by remember { mutableStateOf(1) }

    val extraPrice = remember(selectedSize) {
        when (selectedSize.trim().uppercase()) {
            "2XL", "XXL" -> 20.0
            "3XL", "XXXL" -> 40.0
            "4XL", "XXXXL" -> 60.0
            else -> 0.0
        }
    }
    val unitPrice = product.price + extraPrice
    val totalPrice = unitPrice * qty

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyDark)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "ราคาตัวละ ฿${unitPrice.toInt()}",
                                color = IndigoPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (extraPrice > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "ไซส์พิเศษ +฿${extraPrice.toInt()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Size Selection Header with Guide Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("เลือกขนาดไซส์เสื้อ:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        "📏 ดูตารางเทียบไซส์",
                        color = IndigoPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenSizeGuide() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableSizes.forEach { s ->
                        val isPicked = selectedSize == s
                        Surface(
                            color = if (isPicked) IndigoPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectedSize = s }
                        ) {
                            Text(
                                s,
                                color = if (isPicked) Color.White else NavyDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quantity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("จำนวนที่ต้องการสั่งจอง:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(onClick = { if (qty > 1) qty-- }) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "$qty",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        FilledTonalIconButton(onClick = { qty++ }) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            onAdd(CartItem(product, selectedSize, selectedColor, qty))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "เพิ่มลงตะกร้า (฿${totalPrice.toInt()})",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ปิด")
                    }
                }
            }
        }
    }
}

@Composable
fun SizeGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ตารางขนาดและสัดส่วนไซส์เสื้อ", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = NavyDark)
                        Text("เสื้อสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ", fontSize = 11.sp, color = IndigoPrimary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table Header
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                        Text("ไซส์ (Size)", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("รอบอก (นิ้ว)", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("ความยาว (นิ้ว)", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("หมายเหตุ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    }
                }

                val sizeData = listOf(
                    listOf("SS", "34\"", "25\"", "ราคาปกติ"),
                    listOf("S", "36\"", "26\"", "ราคาปกติ"),
                    listOf("M", "38\"", "27\"", "ราคาปกติ"),
                    listOf("L", "40\"", "28\"", "ราคาปกติ"),
                    listOf("XL", "42\"", "29\"", "ราคาปกติ"),
                    listOf("2XL", "44\"", "30\"", "+฿20"),
                    listOf("3XL", "46\"", "31\"", "+฿40"),
                    listOf("4XL", "48\"", "32\"", "+฿60")
                )

                sizeData.forEachIndexed { idx, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White)
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(row[0], fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark, modifier = Modifier.weight(1f))
                        Text(row[1], fontSize = 12.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f))
                        Text(row[2], fontSize = 12.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f))
                        Text(
                            row[3],
                            fontSize = 11.sp,
                            fontWeight = if (row[3].startsWith("+")) FontWeight.Bold else FontWeight.Normal,
                            color = if (row[3].startsWith("+")) Color(0xFFD97706) else Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "💡 คำแนะนำ: สำหรับเสื้อทรงใส่สบายหรือต้องการทรงหลวม แนะนำให้เลือกเผื่อขนาด 1 ไซส์จากรอบอกปกติครับ",
                        fontSize = 11.sp,
                        color = IndigoPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("เข้าใจแล้ว ปิดหน้าต่าง", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
