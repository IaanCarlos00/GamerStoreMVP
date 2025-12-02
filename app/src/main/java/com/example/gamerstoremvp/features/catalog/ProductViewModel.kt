package com.example.gamerstoremvp.features.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamerstoremvp.models.Product
import com.example.gamerstoremvp.features.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * El estado de la UI ahora espera una LISTA de productos en caso de éxito.
 */
sealed interface ProductListUiState {
    data class Success(val products: List<Product>) : ProductListUiState // <-- CORRECCIÓN: Espera una lista.
    data class Error(val message: String) : ProductListUiState
    object Loading : ProductListUiState
}

class ProductViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        _uiState.value = ProductListUiState.Loading
        viewModelScope.launch {
            try {
                // La API ahora devuelve la lista directamente.
                val productList = RetrofitClient.instance.getProducts()
                // Pasamos la lista al estado Success.
                _uiState.value = ProductListUiState.Success(productList) // <-- CORRECCIÓN: Se pasa la lista.
            } catch (e: IOException) {
                _uiState.value = ProductListUiState.Error("Error de red: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = ProductListUiState.Error("Error desconocido: ${e.message}")
            }
        }
    }
}
