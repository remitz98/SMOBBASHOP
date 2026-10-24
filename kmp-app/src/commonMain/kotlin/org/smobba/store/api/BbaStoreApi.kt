package org.smobba.store.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.smobba.store.models.*

class BbaStoreApi(private val baseUrl: String = "https://bba-store.smobba.workers.dev") {

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    suspend fun getProducts(): List<Product> {
        return try {
            val response = client.get("$baseUrl/api/products")
            val body = response.body<ProductsResponse>()
            body.products
        } catch (e: Exception) {
            println("Error fetching products: ${e.message}")
            emptyList()
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
            val response = client.get("$baseUrl/api/orders/track") {
                parameter("studentId", studentId)
                parameter("pin", pin)
            }
            val body = response.body<TrackOrdersResponse>()
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

    suspend fun getAdminData(): AdminDataResponse {
        return try {
            val response = client.get("$baseUrl/api/admin/data")
            response.body<AdminDataResponse>()
        } catch (e: Exception) {
            AdminDataResponse(success = false, message = e.message ?: "Network error")
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String, adminNotes: String = ""): Boolean {
        return try {
            val response = client.post("$baseUrl/api/admin/order/status") {
                contentType(ContentType.Application.Json)
                setBody(UpdateStatusRequest(orderId, status, adminNotes))
            }
            val body = response.body<SimpleResponse>()
            body.success
        } catch (e: Exception) {
            false
        }
    }
}
