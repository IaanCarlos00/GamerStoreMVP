package com.example.gamerstoremvp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importaciones de tu tema y datos
import com.example.gamerstoremvp.ColorAccentBlue
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Product
import com.example.gamerstoremvp.Roboto
import com.example.gamerstoremvp.formatPrice
// Importa el gestor de cupones
import com.example.gamerstoremvp.CouponManager


@Composable
fun CartScreen(
    cart: Map<Product, Int>,
    onRemoveFromCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    onNavigateToCheckout: () -> Unit,
    userEmail: String // Parámetro obligatorio para el descuento
) {
    // 💡 LÓGICA DE DESCUENTO AUTOMÁTICO (CORREGIDA PARA INT) 💡
    val couponManager = remember { CouponManager() }

    // 1. Calcular Subtotal como Int (ya que Product.price es Int)
    val subtotal: Int = cart.entries.sumOf { it.key.price * it.value }

    // 2. Calcular Descuento (usando Double para precisión)
    val discountAsDouble: Double = remember(subtotal, userEmail) {
        // Pasamos el subtotal como Double para el cálculo del 20%
        couponManager.calculateAutomaticDiscount(subtotal.toDouble(), userEmail)
    }

    // 3. Convertir el descuento a Int para restarlo y formatearlo
    val discountValue: Int = discountAsDouble.toInt()

    // 4. Calcular Total (Int - Int = Int)
    val totalPrice = subtotal - discountValue

    // 💡 FIN LÓGICA DE DESCUENTO 💡

    var productToRemove by remember { mutableStateOf<Product?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
                    CartItemRow(
                        product = product,
                        quantity = quantity,
                        onDecreaseQuantity = {
                            if (quantity == 1) {
                                productToRemove = product
                            } else {
                                onDecreaseQuantity(product)
                            }
                        },
                        onRemoveFromCart = { onRemoveFromCart(product) },
                        onIncreaseQuantity = { onIncreaseQuantity(product) },
                        onProductClick = { onProductClick(product) }
                    )
                }
            }

            // --- Card de Resumen de Compra ---
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

                    // ⬇️ SECCIÓN DE CUPÓN MANUAL ELIMINADA ⬇️

                    // ⬇️ RESUMEN DE PRECIOS (AHORA USA INT) ⬇️
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", fontFamily = Roboto, color = ColorTextPrimary)
                        // Llama a formatPrice(Int) -> OK
                        Text(text = formatPrice(price = subtotal), fontFamily = Orbitron, color = ColorTextPrimary)
                    }

                    if (discountValue > 0) { // Comparamos con Int
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dscto. DuocUC (20%):", fontFamily = Roboto, color = Color.Red.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                            // Llama a formatPrice(Int) -> OK
                            Text(text = "- ${formatPrice(price = discountValue)}", fontFamily = Orbitron, color = Color.Red.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(color = ColorTextSecondary.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total a Pagar:", fontFamily = Roboto, color = ColorTextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        // Llama a formatPrice(Int) -> OK
                        Text(formatPrice(totalPrice), fontFamily = Orbitron, color = ColorAccentNeon, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // ⬆️ FIN RESUMEN DE PRECIOS ⬆️

                    // Botón Finalizar Compra
                    Button(
                        onClick = { onNavigateToCheckout() },
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

    // --- (Diálogos de confirmación) ---
    if (productToRemove != null) {
        val product = productToRemove!!
        AlertDialog(
            onDismissRequest = { productToRemove = null },
            title = { Text("Eliminar Producto", fontFamily = Orbitron, color = ColorTextPrimary) },
            text = { Text("¿Estás seguro de que quieres eliminar '${product.name}' del carrito?", fontFamily = Roboto, color = ColorTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFromCart(product)
                        productToRemove = null
                        showSuccessDialog = true
                    }
                ) {
                    Text("SÍ, ELIMINAR", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productToRemove = null }
                ) {
                    Text("NO, CONSERVAR", color = ColorAccentBlue)
                }
            },
            containerColor = Color.DarkGray
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Éxito", tint = ColorAccentNeon, modifier = Modifier.size(48.dp)) },
            title = { Text("Producto Eliminado", fontFamily = Orbitron, color = ColorTextPrimary) },
            text = { Text("El producto ha sido eliminado con éxito.", fontFamily = Roboto, color = ColorTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text("OK", color = ColorAccentBlue)
                }
            },
            containerColor = Color.DarkGray
        )
    }
} // <-- ESTA LLAVE ARREGLA EL ERROR 'Expecting }' y 'Unresolved reference'


@Composable
fun CartItemRow(
    product: Product,
    quantity: Int,
    onRemoveFromCart: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onProductClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = product.imageResId),
            contentDescription = product.name,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onProductClick() },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProductClick() }
        ) {
            Text(
                product.name,
                fontFamily = Roboto,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2
            )
            Text(
                // (product.price * quantity) es (Int * Int), lo cual da Int.
                // formatPrice(Int) -> OK
                formatPrice(product.price * quantity),
                fontFamily = Orbitron,
                color = ColorAccentBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onDecreaseQuantity() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.DarkGray)
                        .size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, "Quitar uno", tint = ColorTextPrimary)
                }

                Text(
                    text = "$quantity",
                    fontFamily = Roboto,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = { onIncreaseQuantity() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ColorAccentBlue)
                        .size(28.dp)
                ) {
                    Icon(Icons.Default.Add, "Añadir uno", tint = ColorTextPrimary)
                }
            }

            IconButton(
                onClick = { onRemoveFromCart() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.8f)
                )
            }
        }
    }
}