package org.smobba.store.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val productId: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val stock: Int = 0,
    val imageUrl: String = "",
    val description: String = "",
    val status: String = "เปิดพรีออเดอร์"
)

@Serializable
data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val size: String = "",
    val color: String = "",
    val qty: Int = 1,
    val price: Double = 0.0,
    val subtotal: Double = 0.0
)

@Serializable
data class Order(
    val orderId: String = "",
    val orderDate: String = "",
    val studentId: String = "",
    val customerName: String = "",
    val studentEmail: String = "",
    val phone: String = "",
    val itemsSummary: String = "",
    val totalAmount: Double = 0.0,
    val slipUrl: String = "",
    val status: String = "",
    val pickupLocation: String = "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
    val adminNotes: String = ""
)

data class CartItem(
    val product: Product,
    val selectedSize: String,
    val selectedColor: String,
    var qty: Int
) {
    val subtotal: Double
        get() = product.price * qty
}

@Serializable
data class ProductsResponse(
    val success: Boolean = false,
    val products: List<Product> = emptyList(),
    val message: String? = null
)

@Serializable
data class CreateOrderRequest(
    val customerName: String,
    val studentId: String,
    val studentEmail: String,
    val phone: String,
    val studentPin: String,
    val items: List<OrderItem>,
    val paymentMethod: String = "พร้อมเพย์ (QR PromptPay)",
    val pickupLocation: String = "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
    val slipUrl: String = ""
)

@Serializable
data class CreateOrderResponse(
    val success: Boolean = false,
    val orderId: String = "",
    val message: String = ""
)

@Serializable
data class TrackOrdersResponse(
    val success: Boolean = false,
    val orders: List<Order> = emptyList(),
    val message: String = ""
)

@Serializable
data class AdminLoginRequest(
    val pin: String
)

@Serializable
data class AdminLoginResponse(
    val success: Boolean = false,
    val message: String = ""
)

@Serializable
data class AdminStats(
    val totalSales: Double = 0.0,
    val pendingCount: Int = 0,
    val paidCount: Int = 0,
    val readyCount: Int = 0,
    val completedCount: Int = 0
)

@Serializable
data class AdminDataResponse(
    val success: Boolean = false,
    val stats: AdminStats? = null,
    val orders: List<Order> = emptyList(),
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val message: String = ""
)

@Serializable
data class UpdateStatusRequest(
    val orderId: String,
    val status: String,
    val adminNotes: String = ""
)

@Serializable
data class SimpleResponse(
    val success: Boolean = false,
    val message: String = ""
)
