package com.example.gamerstoremvp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importaciones de datos y tema
import com.example.gamerstoremvp.ColorAccentBlue
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Product
import com.example.gamerstoremvp.Roboto
import com.example.gamerstoremvp.formatPrice

/**
 * Pantalla del Carrito de Compras: Muestra resumen y total.
 */
@Composable
fun CartScreen(
    cart: Map<Product, Int>,
    onRemoveFromCart: (Product) -> Unit // Parámetro para la función de eliminación
) {
    val totalPrice = cart.entries.sumOf { it.key.price * it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu Carrito Level-Up",
            fontFamily = Orbitron,
            color = ColorTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "El carrito está vacío.\n¡Es hora de subir de nivel!",
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart.entries.toList()) { (product, quantity) ->
                    // Pasamos la función de eliminación a cada fila del carrito
                    CartItemRow(
                        product = product,
                        quantity = quantity,
                        onRemoveFromCart = onRemoveFromCart
                    )
                }
            }

            // Resumen de la compra
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RESUMEN DE COMPRA",
                        fontFamily = Orbitron,
                        color = ColorAccentNeon,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = ColorTextSecondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total a Pagar:", fontFamily = Roboto, color = ColorTextPrimary, fontWeight = FontWeight.Bold)
                        Text(formatPrice(totalPrice), fontFamily = Orbitron, color = ColorAccentNeon, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de Pago
                    Button(
                        onClick = { /* Lógica de checkout */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentNeon),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text("FINALIZAR COMPRA", fontFamily = Orbitron, color = ColorPrimaryBackground, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * Fila individual para un producto en el carrito.
 * ESTA FUNCIÓN FUE CORREGIDA para aceptar y usar onRemoveFromCart.
 */
@Composable
fun CartItemRow(
    product: Product,
    quantity: Int,
    onRemoveFromCart: (Product) -> Unit // Parámetro añadido que faltaba
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, fontFamily = Roboto, color = ColorTextPrimary, fontWeight = FontWeight.Medium)
            Text("Cantidad: $quantity", fontFamily = Roboto, color = ColorTextSecondary, fontSize = 12.sp)
        }
        Text(
            formatPrice(product.price * quantity),
            fontFamily = Orbitron,
            color = ColorAccentBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // Botón Eliminar
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Eliminar",
            tint = Color.Red.copy(alpha = 0.8f),
            modifier = Modifier
                .size(24.dp)
                .clickable { onRemoveFromCart(product) } // Lógica de eliminación conectada
        )
    }
}