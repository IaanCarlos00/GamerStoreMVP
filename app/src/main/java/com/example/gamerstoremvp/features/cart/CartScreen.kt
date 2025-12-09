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
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gamerstoremvp.core.coupon.CouponManager
import com.example.gamerstoremvp.core.theme.*
import com.example.gamerstoremvp.features.catalog.ProductListUiState
import com.example.gamerstoremvp.features.catalog.ProductViewModel
import com.example.gamerstoremvp.models.Product

@Composable
fun CartScreen(
    cart: Map<Int, Int>, // productId -> quantity
    onRemoveFromCart: (Int) -> Unit,
    onIncreaseQuantity: (Int) -> Unit,
    onDecreaseQuantity: (Int) -> Unit,
    onProductClick: (Int) -> Unit,
    onNavigateToCheckout: (Int) -> Unit,
    userEmail: String
) {
    val viewModel: ProductViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val productList = when (uiState) {
        is ProductListUiState.Success -> (uiState as ProductListUiState.Success).products
        else -> emptyList()
    }

    val couponManager = remember { CouponManager() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var manualCouponCode by remember { mutableStateOf("") }
    var appliedManualDiscount by remember { mutableIntStateOf(0) }
    var couponMessage by remember { mutableStateOf("") }

    // ✅ SUBTOTAL DESDE API
    val subtotal: Int = cart.entries.sumOf { (productId, quantity) ->
        val product = productList.firstOrNull { it.id == productId }

        ((product?.price ?: 0.0) * quantity).toInt()
    }

    // ✅ DESCUENTO AUTOMÁTICO DUOC 20%
    val duocDiscount: Int = remember(subtotal, userEmail) {
        if (userEmail.endsWith("@duocuc.cl", ignoreCase = true)) {
            (subtotal * 0.20).toInt()
        } else {
            0
        }
    }

    val tempTotal = subtotal - duocDiscount

    val effectiveManualDiscount =
        if (appliedManualDiscount > tempTotal) tempTotal else appliedManualDiscount

    val totalPrice = (tempTotal - effectiveManualDiscount).coerceAtLeast(0)

    var productToRemove by remember { mutableStateOf<Product?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ✅ TÍTULO CON ESTILO ORIGINAL
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
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart.entries.toList()) { (productId, quantity) ->
                    val product = productList.firstOrNull { it.id == productId } ?: return@items

                    CartItemRow(
                        product = product,
                        quantity = quantity,
                        onDecreaseQuantity = {
                            if (quantity == 1) productToRemove = product
                            else onDecreaseQuantity(productId)
                        },
                        onRemoveFromCart = { onRemoveFromCart(productId) },
                        onIncreaseQuantity = { onIncreaseQuantity(productId) },
                        onProductClick = { onProductClick(productId) }
                    )
                }
            }

            // ✅ CUPÓN MANUAL
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "¿Tienes un código de Puntos Gamer?",
                        color = ColorTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = manualCouponCode,
                            onValueChange = { manualCouponCode = it.uppercase() },
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
                                val discount =
                                    couponManager.calculateManualCoupon(manualCouponCode)
                                if (discount > 0) {
                                    appliedManualDiscount = discount
                                    couponMessage = "¡Cupón aplicado!"
                                    Toast.makeText(
                                        context,
                                        "Descuento aplicado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    appliedManualDiscount = 0
                                    couponMessage = "Código inválido"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorAccentBlue
                            )
                        ) {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = ColorTextPrimary
                            )
                        }
                    }

                    if (couponMessage.isNotEmpty()) {
                        Text(
                            text = couponMessage,
                            color = if (appliedManualDiscount > 0) ColorAccentNeon else Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ✅ RESUMEN DE COMPRA COMPLETO
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", fontFamily = Roboto, color = ColorTextPrimary)
                        Text(
                            formatPrice(subtotal.toDouble()),
                            fontFamily = Orbitron,
                            color = ColorTextPrimary
                        )
                    }

                    if (duocDiscount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Dscto. DuocUC (20%):",
                                fontFamily = Roboto,
                                color = ColorAccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "- ${formatPrice(duocDiscount.toDouble())}",
                                fontFamily = Orbitron,
                                color = ColorAccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (effectiveManualDiscount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Cupón Gamer:",
                                fontFamily = Roboto,
                                color = ColorAccentNeon,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "- ${formatPrice(effectiveManualDiscount.toDouble())}",
                                fontFamily = Orbitron,
                                color = ColorAccentNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        color = ColorTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total a Pagar:",
                            fontFamily = Roboto,
                            color = ColorTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            formatPrice(totalPrice.toDouble()),
                            fontFamily = Orbitron,
                            color = ColorAccentNeon,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToCheckout(totalPrice) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentNeon),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            "FINALIZAR COMPRA",
                            fontFamily = Orbitron,
                            color = ColorPrimaryBackground,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    // ✅ DIÁLOGO ELIMINAR
    if (productToRemove != null) {
        val product = productToRemove!!
        AlertDialog(
            onDismissRequest = { productToRemove = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Eliminar '${product.name}' del carrito?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveFromCart(product.id)
                    productToRemove = null
                    showSuccessDialog = true
                }) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = { productToRemove = null }) { Text("CANCELAR") }
            }
        )
    }

    // ✅ CONFIRMACIÓN
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Producto eliminado") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) { Text("OK") }
            }
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

        Column(modifier = Modifier.weight(1f)) {
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
            IconButton(onClick = onDecreaseQuantity) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
            }

            Text("$quantity", color = Color.White, fontSize = 20.sp)

            IconButton(onClick = onIncreaseQuantity) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }

            IconButton(onClick = onRemoveFromCart) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}
