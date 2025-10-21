package com.example.gamerstoremvp

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale
// 🚨 IMPORTANTE: Necesitas esta importación para que R.drawable funcione.
import com.example.gamerstoremvp.R

// -------------------------------------------------------------------------------------
// 1. CONSTANTES DE COLOR
// -------------------------------------------------------------------------------------

val ColorPrimaryBackground = Color(0xFF000000) // Negro
val ColorAccentBlue = Color(0xFF1E90FF)     // Azul Eléctrico
val ColorAccentNeon = Color(0xFF39FF14)     // Verde Neón
val ColorTextPrimary = Color(0xFFFFFFFF)    // Blanco
val ColorTextSecondary = Color(0xFFD3D3D3)  // Gris Claro

// -------------------------------------------------------------------------------------
// 2. TIPOGRAFÍA
// -------------------------------------------------------------------------------------

val Roboto = FontFamily.Default
val Orbitron = FontFamily.SansSerif


// -------------------------------------------------------------------------------------
// 3. MODELOS DE DATOS (CORREGIDO)
// -------------------------------------------------------------------------------------

/**
 * Representa un producto en el catálogo.
 */
data class Product(
    val code: String,
    val category: String,
    val name: String,
    val price: Int,
    val description: String,
    val rating: Float,
    val reviewCount: Int,
    // ✅ CORREGIDO: Usamos 'imageResId' para que coincida con CatalogScreen
    val imageResId: Int
)

/**
 * Enumeración para simular las diferentes pantallas de navegación en el MVP.
 */
enum class Screen {
    AUTH, CATALOG, CART
}

// Datos de ejemplo simulados para el catálogo (MOCK DATA)
val mockProducts = listOf(
    // ✅ USAMOS R.drawable.* (asume archivos en minúsculas en res/drawable)
    Product("JM001", "Juegos de Mesa", "Catán", 29990, "Un clásico juego de estrategia.", 4.8f, 142, R.drawable.catan),
    Product("AC002", "Accesorios", "Auriculares Gamer HyperX Cloud II", 79990, "Sonido envolvente y comodidad.", 4.9f, 567, R.drawable.hyperx),
    Product("CO001", "Consolas", "PlayStation 5", 549990, "Consola de última generación.", 4.9f, 892, R.drawable.ps5digital),
    Product("SG001", "Sillas Gamers", "Silla Gamer Secretlab Titan", 349990, "Diseñada para el máximo confort.", 4.8f, 456, R.drawable.sillagamer),
    Product("MS001", "Mouse", "Mouse Gamer Logitech G502 HERO", 49990, "Sensor de alta precisión.", 4.7f, 678, R.drawable.hero)
)

// -------------------------------------------------------------------------------------
// 4. UTILITARIOS
// -------------------------------------------------------------------------------------

// Función utilitaria para formatear el precio a CLP
fun formatPrice(price: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return format.format(price).replace("CLP", "CLP").replace("$", "$ ")
}