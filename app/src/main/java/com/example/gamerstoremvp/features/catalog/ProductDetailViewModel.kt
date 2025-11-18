package com.example.gamerstoremvp.features.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamerstoremvp.core.data.ReviewEntity
import com.example.gamerstoremvp.core.data.ReviewRepository
import com.example.gamerstoremvp.core.theme.Review // Usamos el data class simple para la UI
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val reviewRepository: ReviewRepository,
    private val productCode: String
) : ViewModel() {

    // Expone un flujo de reseñas desde la base de datos, mapeado al modelo de la UI
    val reviews: StateFlow<List<Review>> = reviewRepository.getReviewsForProduct(productCode)
        .map { entityList ->
            // Convierte List<ReviewEntity> a List<Review>
            entityList.map { entity ->
                Review(
                    username = entity.username,
                    rating = entity.rating,
                    comment = entity.comment
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Valor inicial mientras carga el flujo
        )

    // Función para añadir una nueva reseña
    fun addReview(username: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val newReview = ReviewEntity(
                productCode = productCode,
                username = username,
                rating = rating,
                comment = comment
            )
            reviewRepository.insertReview(newReview)
        }
    }
}

// Factory para crear el ViewModel con sus dependencias
class ProductDetailViewModelFactory(
    private val reviewRepository: ReviewRepository,
    private val productCode: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductDetailViewModel(reviewRepository, productCode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
