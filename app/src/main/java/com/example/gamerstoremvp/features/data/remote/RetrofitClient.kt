package com.example.gamerstoremvp.features.data.remote

import com.example.gamerstoremvp.models.Product
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Interfaz que define los endpoints de la API.
 */
interface ApiService {
    @GET("079008f2a00eb3872e2b17a9f1a5be24/raw/3215a46da2a6f36b74f25be218c7731ccca46d5c/gistfile1.txt")
    suspend fun getProducts(): List<Product>
}


/**
 * Objeto singleton para crear y proveer la instancia de Retrofit.
 */
object RetrofitClient {

    private const val BASE_URL = "https://gist.githubusercontent.com/IaanCarlos00/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
