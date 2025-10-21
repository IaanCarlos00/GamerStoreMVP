package com.example.gamerstoremvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Importaciones de tus archivos y datos
import com.example.gamerstoremvp.ui.theme.GamerStoreMVPTheme // Asegúrate que el nombre del paquete del tema sea correcto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Se usa el tema personalizado para aplicar estilos consistentes.
            GamerStoreMVPTheme {
                GamerStoreApp()
            }
        }
    }
}

/**
 * Componente raíz que maneja el estado global (navegación, carrito) y la estructura de la app.
 * Actúa como el "cerebro" de la aplicación.
 */
@Composable
fun GamerStoreApp() {
    // [ESTADO 1] Controla qué pantalla se muestra actualmente.
    var currentScreen by remember { mutableStateOf(Screen.CATALOG) }

    // [ESTADO 2] Guarda los productos en el carrito. Usamos un `mutableStateMapOf`
    // porque es eficiente para actualizar, añadir o eliminar items.
    val shoppingCart = remember { mutableStateMapOf<Product, Int>() }

    // [ESTADO 3] Simula si el usuario ha iniciado sesión.
    var isAuthenticated by remember { mutableStateOf(true) }

    // --- LÓGICA DE NEGOCIO ---

    // Función para AÑADIR un producto. Se la pasaremos a CatalogScreen.
    val onAddToCart: (Product) -> Unit = { product ->
        // Si el producto ya existe, aumenta su cantidad; si no, lo añade con cantidad 1.
        shoppingCart[product] = (shoppingCart[product] ?: 0) + 1
    }

    // Función para ELIMINAR un producto. Se la pasaremos a CartScreen.
    val onRemoveFromCart: (Product) -> Unit = { product ->
        shoppingCart.remove(product)
    }

    // Función para manejar el inicio de sesión exitoso.
    val onAuthSuccess: () -> Unit = {
        isAuthenticated = true
        currentScreen = Screen.CATALOG
    }

    // --- ESTRUCTURA DE LA UI ---

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorPrimaryBackground // Fondo oscuro principal de la app
    ) {
        Scaffold(
            topBar = {
                // La barra superior solo aparece si el usuario está autenticado.
                if (isAuthenticated) {
                    GamerStoreTopBar(
                        onNavigateToCart = { currentScreen = Screen.CART },
                        onNavigateToCatalog = { currentScreen = Screen.CATALOG },
                        // La cantidad de items en el carrito se calcula aquí
                        cartItemCount = shoppingCart.values.sum(),
                        onLogout = {
                            isAuthenticated = false
                            currentScreen = Screen.AUTH
                            shoppingCart.clear() // Limpiamos el carrito al cerrar sesión
                        }
                    )
                }
            },
            floatingActionButton = {
                // El botón flotante solo aparece en las pantallas principales.
                if (isAuthenticated) {
                    FloatingActionButton(
                        onClick = { println("Navegando a Soporte Técnico...") },
                        containerColor = ColorAccentNeon,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Support,
                                contentDescription = "Soporte Técnico",
                                tint = ColorPrimaryBackground
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Soporte Técnico",
                                color = ColorPrimaryBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            containerColor = ColorPrimaryBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Padding importante para que el contenido no quede debajo de la TopBar
            ) {
                // --- NAVEGACIÓN ---
                // El `when` decide qué pantalla mostrar basándose en el estado `currentScreen`.
                when (currentScreen) {
                    Screen.AUTH -> AuthScreen(onAuthSuccess = onAuthSuccess)

                    Screen.CATALOG -> CatalogScreen(
                        products = mockProducts,
                        onAddToCart = onAddToCart // Pasamos la función de añadir
                    )

                    Screen.CART -> CartScreen(
                        cart = shoppingCart,
                        onRemoveFromCart = onRemoveFromCart // Pasamos la función de eliminar
                    )
                }
            }
        }
    }
}