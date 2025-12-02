package com.example.gamerstoremvp.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import com.example.gamerstoremvp.R

// Colores de la app
val ColorPrimaryBackground = Color(0xFF000000)
val ColorAccentBlue = Color(0xFF1E90FF)
val ColorAccentNeon = Color(0xFF39FF14)
val ColorTextPrimary = Color(0xFFFFFFFF)
val ColorTextSecondary = Color(0xFFD3D3D3)

// Tipografía
val Roboto = FontFamily.Default
val Orbitron = FontFamily.SansSerif

/**
 * Representa una reseña de usuario.
 */
data class Review(
    val username: String,
    val rating: Int,
    val comment: String
)

/**
 * Representa un usuario.
 */
data class User(
    val id: String = "u${System.currentTimeMillis()}",
    var name: String,
    var email: String,
    var password: String,
    var phone: String,
    var address: String,
    var profileImageResId: Int? = null,
    var levelUpPoints: Int = 0,
    val referralCode: String = UUID.randomUUID().toString().take(8)
)

/**
 * Representa un ítem específico dentro de un pedido.
 */
data class OrderItem(
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Int
)

/**
 * Representa un pedido completo realizado por un usuario.
 */
data class Order(
    val id: String = "ord_${System.currentTimeMillis()}",
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<OrderItem>,
    val totalAmount: Int,
    val userId: String
)

/**
 * Define las diferentes pantallas/rutas de la aplicación.
 */
enum class Screen {
    AUTH,
    CATALOG,
    CART,
    PRODUCT_DETAIL,
    PROFILE,
    CHECKOUT,
    ORDERS,
    ABOUT_US,
    EVENTS_MAP,
}

// Función para formatear el precio
fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return format.format(price).replace(",00", "").replace("$", "$ ")
}
