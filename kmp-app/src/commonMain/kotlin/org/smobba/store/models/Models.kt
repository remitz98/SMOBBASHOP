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
    val status: String = "พร้อมจำหน่าย",
    val sizeGuide: String = ""
)

@Serializable
data class StoreResponse(
    val success: Boolean = false,
    val storeName: String = "SMO BBA SHOP",
    val clubLocation: String = "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
    val clubSchedule: String = "วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.",
    val promptPay: String = "004999245434427",
    val bankDetails: String = "",
    val allowedDomain: String = "@ubru.ac.th",
    val categories: List<String> = emptyList(),
    val products: List<Product> = emptyList()
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
    val unitPrice: Double
        get() {
            val extra = when (selectedSize.trim().uppercase()) {
                "2XL", "XXL" -> 20.0
                "3XL", "XXXL" -> 40.0
                "4XL", "XXXXL" -> 60.0
                else -> 0.0
            }
            return product.price + extra
        }

    val subtotal: Double
        get() = unitPrice * qty
}

@Serializable
data class CreateOrderRequest(
    val customerName: String = "",
    val studentId: String = "",
    val studentEmail: String = "",
    val phone: String = "",
    val studentPin: String = "",
    val items: List<OrderItem> = emptyList(),
    val paymentMethod: String = "พร้อมเพย์ (QR PromptPay)",
    val pickupLocation: String = "ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
    val slipUrl: String = "",
    val notes: String = ""
)

@Serializable
data class CreateOrderResponse(
    val success: Boolean = false,
    val orderId: String = "",
    val totalAmount: Double = 0.0,
    val message: String = ""
)

@Serializable
data class MyOrdersRequest(
    val studentId: String = "",
    val pin: String = ""
)

@Serializable
data class MyOrdersResponse(
    val success: Boolean = false,
    val orders: List<Order> = emptyList(),
    val message: String = ""
)

@Serializable
data class AdminLoginRequest(
    val pin: String = ""
)

@Serializable
data class AdminLoginResponse(
    val success: Boolean = false,
    val message: String = ""
)

@Serializable
data class AdminDataRequest(
    val pin: String = ""
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
data class AdminUpdateStatusRequest(
    val pin: String = "",
    val orderId: String = "",
    val newStatus: String = "",
    val adminNotes: String = ""
)

@Serializable
data class SimpleResponse(
    val success: Boolean = false,
    val message: String = ""
)
