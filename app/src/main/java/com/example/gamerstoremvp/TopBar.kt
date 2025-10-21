package com.example.gamerstoremvp

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importaciones de datos y tema
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Roboto

/**
 * Barra de aplicación personalizada con el tema gamer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamerStoreTopBar(
    onNavigateToCart: () -> Unit,
    onNavigateToCatalog: () -> Unit, // Aunque no se usa aquí, es bueno tenerlo
    cartItemCount: Int,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEVEL-UP",
                    fontFamily = Orbitron,
                    fontWeight = FontWeight.Black,
                    color = ColorAccentNeon,
                    fontSize = 20.sp,
                )
                Text(
                    text = " Gaming Store",
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        actions = {
            // --- CAMBIO PRINCIPAL AQUÍ ---
            // 1. Ícono del Carrito con Insignia (Badge) para mostrar la cantidad
            BadgedBox(
                badge = {
                    // La insignia solo se muestra si hay productos en el carrito
                    if (cartItemCount > 0) {
                        Badge {
                            Text(text = "$cartItemCount")
                        }
                    }
                }
            ) {
                // 2. Hacemos que el ícono sea un botón clicable
                IconButton(onClick = onNavigateToCart) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Carrito de Compras",
                        tint = ColorTextPrimary
                    )
                }
            }

            // 3. Botón para Cerrar Sesión
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    tint = ColorTextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ColorPrimaryBackground,
            titleContentColor = ColorAccentNeon
        )
    )
}