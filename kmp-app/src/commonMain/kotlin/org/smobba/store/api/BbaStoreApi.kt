package org.smobba.store.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.smobba.store.models.*

class BbaStoreApi(val baseUrl: String = "https://bba-store.smobba.workers.dev") {

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    fun resolveImageUrl(url: String): String {
        if (url.isBlank()) return ""
        return if (url.startsWith("/")) baseUrl + url else url
    }

    suspend fun getStoreData(): StoreResponse {
        return try {
            val response = client.get("$baseUrl/api/store")
            val raw = response.body<StoreResponse>()
            val fixedProducts = raw.products.map { p ->
                p.copy(imageUrl = resolveImageUrl(p.imageUrl))
            }
            raw.copy(products = fixedProducts)
        } catch (e: Exception) {
            println("Error fetching store: ${e.message}")
            StoreResponse(success = false)
        }
    }

    suspend fun createOrder(req: CreateOrderRequest): CreateOrderResponse {
        return try {
            val response = client.post("$baseUrl/api/orders") {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            response.body<CreateOrderResponse>()
        } catch (e: Exception) {
            CreateOrderResponse(success = false, message = e.message ?: "Failed to connect to server")
        }
    }

    suspend fun trackOrders(studentId: String, pin: String): List<Order> {
        return try {
            val response = client.post("$baseUrl/api/my-orders") {
                contentType(ContentType.Application.Json)
                setBody(MyOrdersRequest(studentId, pin))
            }
            val body = response.body<MyOrdersResponse>()
            body.orders
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun adminLogin(pin: String): Boolean {
        return try {
            val response = client.post("$baseUrl/api/admin/login") {
                contentType(ContentType.Application.Json)
                setBody(AdminLoginRequest(pin))
            }
            val body = response.body<AdminLoginResponse>()
            body.success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAdminData(pin: String): AdminDataResponse {
        return try {
            val response = client.post("$baseUrl/api/admin/data") {
                contentType(ContentType.Application.Json)
                setBody(AdminDataRequest(pin))
            }
            val raw = response.body<AdminDataResponse>()
            val fixedProducts = raw.products.map { p ->
                p.copy(imageUrl = resolveImageUrl(p.imageUrl))
            }
            raw.copy(products = fixedProducts)
        } catch (e: Exception) {
            AdminDataResponse(success = false, message = e.message ?: "Network error")
        }
    }

    suspend fun updateOrderStatus(pin: String, orderId: String, status: String, adminNotes: String = ""): Boolean {
        return try {
            val response = client.post("$baseUrl/api/admin/orders/status") {
                contentType(ContentType.Application.Json)
                setBody(AdminUpdateStatusRequest(pin, orderId, status, adminNotes))
            }
            val body = response.body<SimpleResponse>()
            body.success
        } catch (e: Exception) {
            false
        }
    }
}
