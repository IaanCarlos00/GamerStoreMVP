package com.example.gamerstoremvp.features.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamerstoremvp.core.data.ReviewEntity
import com.example.gamerstoremvp.core.data.ReviewRepository
import com.example.gamerstoremvp.core.theme.Review
import com.example.gamerstoremvp.models.Product
import com.example.gamerstoremvp.features.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

// Represents the state for the product detail view
sealed interface ProductDetailUiState {
    data class Success(val product: Product) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
    object Loading : ProductDetailUiState
}

class ProductDetailViewModel(
    private val reviewRepository: ReviewRepository,
    private val productCode: String // This is the product ID
) : ViewModel() {

    // State for the product details
    private val _productState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val product: StateFlow<ProductDetailUiState> = _productState.asStateFlow()

    // State for the reviews (existing logic)
    val reviews: StateFlow<List<Review>> = reviewRepository.getReviewsForProduct(productCode)
        .map { entityList ->
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
            initialValue = emptyList()
        )

    init {
        fetchProductDetails()
    }

    private fun fetchProductDetails() {
        _productState.value = ProductDetailUiState.Loading
        viewModelScope.launch {
            val productId = productCode.toIntOrNull()
            if (productId == null) {
                _productState.value = ProductDetailUiState.Error("ID de producto inválido.")
                return@launch
            }

            try {
                // CORRECCIÓN 1: La llamada ahora devuelve la lista directamente.
                val productList = RetrofitClient.instance.getProducts()
                // CORRECCIÓN 2: Buscamos el producto en la lista.
                val product = productList.find { it.id == productId }

                if (product != null) {
                    _productState.value = ProductDetailUiState.Success(product)
                } else {
                    _productState.value = ProductDetailUiState.Error("Producto no encontrado.")
                }
            } catch (e: IOException) {
                _productState.value = ProductDetailUiState.Error("Error de red: ${e.message}")
            } catch (e: Exception) {
                _productState.value = ProductDetailUiState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    // Function to add a new review (existing logic)
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

// Factory to create the ViewModel with its dependencies
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
