package org.smobba.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import org.smobba.store.models.CartItem
import org.smobba.store.models.Product
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark

@Composable
fun StoreScreen(
    products: List<Product>,
    cart: List<CartItem>,
    onAddToCart: (CartItem) -> Unit,
    onOpenCart: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }
    var viewingProduct by remember { mutableStateOf<Product?>(null) }

    val categories = remember(products) {
        listOf("ทั้งหมด") + products.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "ทั้งหมด") products else products.filter { it.category == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header Banner
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "เปิดจองเสื้อสโมสรนักศึกษา BBA UBRU",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "สั่งจองออนไลน์ ชำระผ่านพร้อมเพย์ รับเสื้อได้ที่ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Categories Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Products Grid
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("กำลังโหลดรายการเสื้อ...", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                text = { Text("ดูตะกร้า (${cart.sumOf { it.qty }} ตัว)") },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }
    }

    // Product Detail & Add to Cart Dialog
    viewingProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { viewingProduct = null },
            onAdd = { item ->
                onAddToCart(item)
                viewingProduct = null
            }
        )
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("รูปภาพเสื้อ", color = Color.Gray)
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        product.category.ifBlank { "เสื้อกิจกรรม" },
                        color = IndigoPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "฿${product.price}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = IndigoPrimary
                    )
                    Button(
                        onClick = onSelect,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                    ) {
                        Text("สั่งจอง", fontSize = 12.sp)
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
    onAdd: (CartItem) -> Unit
) {
    var selectedSize by remember { mutableStateOf(product.sizes.firstOrNull() ?: "L") }
    var selectedColor by remember { mutableStateOf(product.colors.firstOrNull() ?: "สีกรมท่า") }
    var qty by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("ราคาตัวละ ฿${product.price}", color = IndigoPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Size options
                Text("เลือกไซส์:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (product.sizes.ifEmpty { listOf("S", "M", "L", "XL", "2XL") }).forEach { s ->
                        OutlinedButton(
                            onClick = { selectedSize = s },
                            colors = if (selectedSize == s) ButtonDefaults.buttonColors(containerColor = IndigoPrimary) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(s, color = if (selectedSize == s) Color.White else Color.Black, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("จำนวน:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (qty > 1) qty-- }) { Text("-", fontSize = 20.sp) }
                        Text("$qty", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { qty++ }) { Text("+", fontSize = 20.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            onAdd(CartItem(product, selectedSize, selectedColor, qty))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("เพิ่มลงตะกร้า (฿${product.price * qty})")
                    }
                    OutlinedButton(onClick = onDismiss) { Text("ปิด") }
                }
            }
        }
    }
}
