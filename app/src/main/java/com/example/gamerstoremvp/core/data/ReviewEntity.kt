package com.example.gamerstoremvp.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productCode: String, // Para saber a qué producto pertenece
    val username: String,
    val rating: Int,
    val comment: String
)
