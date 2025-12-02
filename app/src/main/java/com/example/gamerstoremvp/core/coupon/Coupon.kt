package com.example.gamerstoremvp.core.coupon

data class Coupon(
    val code: String,
    val type: CouponType,
    val value: Double
)

enum class CouponType {
    FIXED_AMOUNT,
    PERCENTAGE
}