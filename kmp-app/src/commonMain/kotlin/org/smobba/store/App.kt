package org.smobba.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.smobba.store.api.BbaStoreApi
import org.smobba.store.models.CartItem
import org.smobba.store.models.Order
import org.smobba.store.models.Product
import org.smobba.store.models.StoreResponse
import org.smobba.store.theme.BbaStoreTheme
import org.smobba.store.theme.EmeraldSuccess
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark
import org.smobba.store.ui.*

enum class AppScreen {
    STORE, TRACK, ADMIN
}

@Composable
fun App() {
    val api = remember { BbaStoreApi() }
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppScreen.STORE) }
    var storeData by remember { mutableStateOf<StoreResponse?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoadingStore by remember { mutableStateOf(true) }
    var storeLoadError by remember { mutableStateOf<String?>(null) }

    var cart by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var showCartDialog by remember { mutableStateOf(false) }
    var activeReceiptOrder by remember { mutableStateOf<Order?>(null) }

    fun loadStore() {
        isLoadingStore = true
        storeLoadError = null
        coroutineScope.launch {
            try {
                val data = api.getStoreData()
                storeData = data
                products = data.products
                if (data.products.isEmpty() && !data.success) {
                    storeLoadError = "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์สโมสรได้ กรุณาตรวจสอบอินเทอร์เน็ตและลองใหม่อีกครั้ง"
                }
            } catch (e: Exception) {
                storeLoadError = e.message ?: "เกิดข้อผิดพลาดในการโหลดข้อมูลสินค้า"
            } finally {
                isLoadingStore = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadStore()
    }

    BbaStoreTheme {
        Scaffold(
            topBar = {
                // Modern Desktop & Mobile Responsive Top Header
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Store Brand Logo & Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { currentScreen = AppScreen.STORE }
                        ) {
                            Surface(
                                color = NavyDark,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "BBA",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    storeData?.storeName?.ifBlank { "SMO BBA SHOP" } ?: "SMO BBA SHOP",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = NavyDark
                                )
                                Text(
                                    "คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
                                    fontSize = 11.sp,
                                    color = IndigoPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Right: Navigation Action Pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Store
                            NavPillButton(
                                title = "เสื้อสโมสร",
                                icon = Icons.Default.ShoppingBag,
                                isSelected = currentScreen == AppScreen.STORE,
                                onClick = { currentScreen = AppScreen.STORE }
                            )

                            // 2. Track
                            NavPillButton(
                                title = "เช็กสถานะ",
                                icon = Icons.Default.History,
                                isSelected = currentScreen == AppScreen.TRACK,
                                onClick = { currentScreen = AppScreen.TRACK }
                            )

                            // 3. Admin
                            NavPillButton(
                                title = "ระบบแอดมิน",
                                icon = Icons.Default.Lock,
                                isSelected = currentScreen == AppScreen.ADMIN,
                                onClick = { currentScreen = AppScreen.ADMIN }
                            )

                            // 4. Cart Button with Badge
                            Surface(
                                color = if (cart.isNotEmpty()) IndigoPrimary else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clickable { showCartDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = if (cart.isNotEmpty()) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "ตะกร้า (${cart.sumOf { it.qty }})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cart.isNotEmpty()) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.STORE -> StoreScreen(
                        storeData = storeData,
                        products = products,
                        isLoading = isLoadingStore,
                        errorMessage = storeLoadError,
                        onRetry = { loadStore() },
                        cart = cart,
                        onAddToCart = { item ->
                            val current = cart.toMutableList()
                            val existing = current.find {
                                it.product.productId == item.product.productId &&
                                it.selectedSize == item.selectedSize &&
                                it.selectedColor == item.selectedColor
                            }
                            if (existing != null) {
                                existing.qty += item.qty
                            } else {
                                current.add(item)
                            }
                            cart = current
                        },
                        onOpenCart = { showCartDialog = true }
                    )
                    AppScreen.TRACK -> TrackScreen(
                        api = api,
                        onViewReceipt = { order -> activeReceiptOrder = order }
                    )
                    AppScreen.ADMIN -> AdminScreen(
                        api = api,
                        onViewReceipt = { order -> activeReceiptOrder = order }
                    )
                }

                // Cart / Checkout Dialog
                if (showCartDialog) {
                    CartAndCheckoutDialog(
                        cart = cart,
                        api = api,
                        onCartChange = { cart = it },
                        onOrderSuccess = { newOrder ->
                            showCartDialog = false
                            activeReceiptOrder = newOrder
                        },
                        onDismiss = { showCartDialog = false }
                    )
                }

                // Official E-Receipt Dialog
                activeReceiptOrder?.let { order ->
                    ReceiptDialog(
                        order = order,
                        onDismiss = { activeReceiptOrder = null }
                    )
                }
            }
        }
    }
}

@Composable
fun NavPillButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFFEEF2FF) else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = if (isSelected) IndigoPrimary else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) IndigoPrimary else Color(0xFF64748B)
            )
        }
    }
}
