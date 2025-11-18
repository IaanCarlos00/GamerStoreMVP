package com.example.gamerstoremvp.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
// Solo necesitamos el ícono del carrito ahora
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Tus importaciones de tema
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.Orbitron
import com.example.gamerstoremvp.core.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamerStoreTopBar(
    onNavigateToCart: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    cartItemCount: Int,
    navigationIcon: @Composable () -> Unit = {} // <-- MODIFICACIÓN 1: Añadir ícono de navegación
    // Se quita onNavigateToProfile de aquí
) {
    TopAppBar(
        title = {
            Row( // El título sigue siendo clickeable para ir al catálogo
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateToCatalog() }
            ) {
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
        // --- MODIFICACIÓN 2: Usar el navigationIcon ---
        navigationIcon = navigationIcon,
        // ---------------------------------------------
        actions = {
            // Ícono del Carrito con Insignia (no cambia)
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge { Text(text = "$cartItemCount") }
                    }
                }
            ) {
                IconButton(onClick = onNavigateToCart) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Carrito de Compras",
                        tint = ColorTextPrimary
                    )
                }
            }
            // --- MODIFICACIÓN 3: Se quita el botón de perfil de aquí ---
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ColorPrimaryBackground,
            titleContentColor = ColorAccentNeon,
            actionIconContentColor = ColorTextPrimary // Asegurar color de íconos
        )
    )
}