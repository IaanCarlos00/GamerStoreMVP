package com.example.gamerstoremvp.models

/**
 * CORRECCIÓN: El campo 'reviews' ahora es una Lista de Strings,
 * para que coincida con el array que envía el JSON.
 */
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val rating: Double,
    val reviews: List<String> // <-- Cambiado de Int a List<String>
)
