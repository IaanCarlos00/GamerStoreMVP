package com.example.gamerstoremvp.core.coupon

data class Coupon(
    val code: String,
    val type: CouponType,
    val value: Double // Puede ser monto fijo o porcentaje
)

enum class CouponType {
    FIXED_AMOUNT, // Monto fijo (ej: 10 €)
    PERCENTAGE    // Porcentaje (ej: 20%)
}