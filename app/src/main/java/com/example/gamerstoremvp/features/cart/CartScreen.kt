package com.example.gamerstoremvp.features.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer // Icono de etiqueta
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
// Importaciones de tu tema y datos
import com.example.gamerstoremvp.core.theme.ColorAccentBlue
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.Orbitron
import com.example.gamerstoremvp.models.Product
import com.example.gamerstoremvp.core.theme.Roboto
import com.example.gamerstoremvp.core.theme.formatPrice
// Importa el gestor de cupones
import com.example.gamerstoremvp.core.coupon.CouponManager


@Composable
fun CartScreen(
    cart: Map<Product, Int>,
    onRemoveFromCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    onNavigateToCheckout: (Int) -> Unit, // Recibe el Total Final
    userEmail: String
) {
    val couponManager = remember { CouponManager() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- ESTADOS PARA CUPÓN MANUAL ---
    var manualCouponCode by remember { mutableStateOf("") }
    var appliedManualDiscount by remember { mutableIntStateOf(0) }
    var couponMessage by remember { mutableStateOf("") }
    // ---------------------------------

    // 1. Calcular Subtotal
    val subtotal: Int = cart.entries.sumOf { (it.key.price * it.value).toInt() }

    // 2. Calcular Descuento Automático (Email)
    val autoDiscountDouble: Double = remember(subtotal, userEmail) {
        couponManager.calculateAutomaticDiscount(subtotal.toDouble(), userEmail)
    }
    val autoDiscountValue: Int = autoDiscountDouble.toInt()

    // 3. Calcular Total preliminar
    val tempTotal = subtotal - autoDiscountValue

    // 4. Ajustar el Descuento Manual (No puede ser mayor que lo que queda por pagar)
    val effectiveManualDiscount = if (appliedManualDiscount > tempTotal) tempTotal else appliedManualDiscount

    // 5. Calcular Total Final
    val totalPrice = (tempTotal - effectiveManualDiscount).coerceAtLeast(0)


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

            // --- SECCIÓN DE CUPÓN MANUAL ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("¿Tienes un código de Puntos Gamer?", color = ColorTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = manualCouponCode,
                            onValueChange = { manualCouponCode = it.uppercase() }, // Auto mayúsculas
                            placeholder = { Text("Ej: LEVELUP5000", color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorTextPrimary,
                                unfocusedTextColor = ColorTextPrimary,
                                focusedBorderColor = ColorAccentBlue,
                                unfocusedBorderColor = Color.Gray
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val discount = couponManager.calculateManualCoupon(manualCouponCode)
                                if (discount > 0) {
                                    appliedManualDiscount = discount
                                    couponMessage = "¡Cupón aplicado!"
                                    Toast.makeText(context, "Descuento de ${formatPrice(discount.toDouble())} aplicado", Toast.LENGTH_SHORT).show()
                                } else {
                                    appliedManualDiscount = 0
                                    couponMessage = "Código inválido"
                                    Toast.makeText(context, "Código inválido", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp) // Misma altura que el TextField
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = ColorTextPrimary)
                        }
                    }
                    if (couponMessage.isNotEmpty()) {
                        Text(
                            text = couponMessage,
                            color = if (appliedManualDiscount > 0) ColorAccentNeon else Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            // -------------------------------

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
                    HorizontalDivider(color = ColorTextSecondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filas de precios
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", fontFamily = Roboto, color = ColorTextPrimary)
                        Text(text = formatPrice(subtotal.toDouble()), fontFamily = Orbitron, color = ColorTextPrimary)
                    }

                    // Fila Descuento Automático
                    if (autoDiscountValue > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dscto. DuocUC (20%):", fontFamily = Roboto, color = ColorAccentBlue, fontWeight = FontWeight.Bold)
                            Text(text = "- ${formatPrice(autoDiscountValue.toDouble())}", fontFamily = Orbitron, color = ColorAccentBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Fila Descuento Manual (Puntos)
                    if (effectiveManualDiscount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cupón Gamer:", fontFamily = Roboto, color = ColorAccentNeon, fontWeight = FontWeight.Bold)
                            Text(text = "- ${formatPrice(effectiveManualDiscount.toDouble())}", fontFamily = Orbitron, color = ColorAccentNeon, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = ColorTextSecondary.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total a Pagar:", fontFamily = Roboto, color = ColorTextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(formatPrice(totalPrice.toDouble()), fontFamily = Orbitron, color = ColorAccentNeon, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Finalizar Compra
                    Button(
                        onClick = { onNavigateToCheckout(totalPrice) },
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

    // --- (Diálogos de confirmación sin cambios) ---
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
}


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
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onProductClick() },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.name,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1
            )
            Text(
                text = formatPrice(product.price),
                color = ColorAccentNeon,
                fontSize = 14.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecreaseQuantity, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Quitar uno", tint = Color.White)
            }
            Text("$quantity", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = onIncreaseQuantity, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Añadir uno", tint = Color.White)
            }
            IconButton(onClick = onRemoveFromCart, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar del carrito", tint = Color.Red)
            }
        }
    }
}
