package com.example.gamerstoremvp.core.coupon

class CouponManager {

    // Reglas de descuento automáticas
    private val DUOCUC_DOMAIN = "@duocuc.cl"
    private val DUOCUC_DISCOUNT_PERCENTAGE = 0.20 // 20%

    /**
     * Calcula el descuento automático basado en el email.
     * @param subtotal El subtotal (viene como Double para precisión)
     * @param userEmail El email del usuario
     * @return El monto del descuento (Double)
     */
    fun calculateAutomaticDiscount(subtotal: Double, userEmail: String): Double {
        if (subtotal <= 0) {
            return 0.0
        }

        // Verifica si el correo termina en el dominio requerido
        val isDuocUser = userEmail.lowercase().trim().endsWith(DUOCUC_DOMAIN)

        return if (isDuocUser) {
            // Aplica el 20% de descuento
            subtotal * DUOCUC_DISCOUNT_PERCENTAGE
        } else {
            0.0 // 0% de descuento para cualquier otro
        }
    }
}