package org.smobba.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.smobba.store.api.BbaStoreApi
import org.smobba.store.models.CartItem
import org.smobba.store.models.Order
import org.smobba.store.models.Product
import org.smobba.store.theme.BbaStoreTheme
import org.smobba.store.theme.IndigoPrimary
import org.smobba.store.theme.NavyDark
import org.smobba.store.ui.*

enum class AppScreen {
    STORE, TRACK, ADMIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val api = remember { BbaStoreApi() }
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppScreen.STORE) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var cart by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    var showCartDialog by remember { mutableStateOf(false) }
    var activeReceiptOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            products = api.getProducts()
        }
    }

    BbaStoreTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "SMO BBA SHOP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                "สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark),
                    actions = {
                        // Navigation Tabs
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.STORE,
                            onClick = { currentScreen = AppScreen.STORE },
                            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = if (currentScreen == AppScreen.STORE) Color.White else Color(0xFF94A3B8)) },
                            label = { Text("เสื้อสโมสร", fontSize = 10.sp, color = if (currentScreen == AppScreen.STORE) Color.White else Color(0xFF94A3B8)) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TRACK,
                            onClick = { currentScreen = AppScreen.TRACK },
                            icon = { Icon(Icons.Default.History, contentDescription = null, tint = if (currentScreen == AppScreen.TRACK) Color.White else Color(0xFF94A3B8)) },
                            label = { Text("เช็กสถานะ", fontSize = 10.sp, color = if (currentScreen == AppScreen.TRACK) Color.White else Color(0xFF94A3B8)) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ADMIN,
                            onClick = { currentScreen = AppScreen.ADMIN },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = if (currentScreen == AppScreen.ADMIN) Color.White else Color(0xFF94A3B8)) },
                            label = { Text("แอดมิน", fontSize = 10.sp, color = if (currentScreen == AppScreen.ADMIN) Color.White else Color(0xFF94A3B8)) }
                        )
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.STORE -> StoreScreen(
                        products = products,
                        cart = cart,
                        onAddToCart = { item ->
                            val current = cart.toMutableList()
                            val existing = current.find { it.product.productId == item.product.productId && it.selectedSize == item.selectedSize && it.selectedColor == item.selectedColor }
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

                // Official Receipt Dialog
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
