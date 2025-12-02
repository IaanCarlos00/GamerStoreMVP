package com.example.gamerstoremvp.features.data.remote

import com.example.gamerstoremvp.models.Product
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Interfaz que define los endpoints de la API.
 */
interface ApiService {
    /**
     * SOLUCIÓN FINAL: Se necesita la ruta completa en el @GET, incluyendo el nombre del archivo.
     * El error 404 indica que el servidor no encuentra el archivo, por lo que el nombre es crucial.
     */
    @GET("01d2a8b1bd8a80dcd54214cc91d118ae/raw/")
    suspend fun getProducts(): List<Product>
}

/**
 * Objeto singleton para crear y proveer la instancia de Retrofit.
 */
object RetrofitClient {
    // La BASE_URL apunta solo al usuario, como en tu ejemplo.
    private const val BASE_URL = "https://gist.githubusercontent.com/IaanCarlos00/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
