package com.example.gamerstoremvp.core.coupon

class CouponManager {

    companion object {
        private const val DUOCUC_DOMAIN = "@duocuc.cl"
        private const val DUOCUC_DISCOUNT_PERCENTAGE = 0.20 // 20%
        private const val COUPON_PREFIX = "LEVELUP" // Prefijo para cupones manuales
    }

    /**
     * Calcula el descuento automático (20%) si el correo es de DuocUC.
     */
    fun calculateAutomaticDiscount(subtotal: Double, userEmail: String?): Double {
        if (subtotal <= 0 || userEmail.isNullOrEmpty()) return 0.0

        val normalizedEmail = userEmail.lowercase().trim()
        val isDuocUser = normalizedEmail.endsWith(DUOCUC_DOMAIN)

        return if (isDuocUser) {
            subtotal * DUOCUC_DISCOUNT_PERCENTAGE
        } else {
            0.0
        }
    }

    /**
     * --- ESTA ES LA FUNCIÓN QUE TE FALTABA ---
     * Valida y extrae el monto de un cupón manual (ej: "LEVELUP5000" -> 5000).
     * Retorna 0 si el cupón es inválido o no tiene el formato correcto.
     */
    fun calculateManualCoupon(couponCode: String): Int {
        val code = couponCode.trim().uppercase()

        // Verifica si empieza con "LEVELUP"
        if (code.startsWith(COUPON_PREFIX)) {
            // Intenta extraer los números después del prefijo
            val amountString = code.removePrefix(COUPON_PREFIX)
            // Si es un número válido, lo retorna. Si no, retorna 0.
            return amountString.toIntOrNull() ?: 0
        }
        return 0
    }
}