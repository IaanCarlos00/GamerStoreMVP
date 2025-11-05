package com.example.gamerstoremvp

// --- ¡NUEVAS IMPORTACIONES PARA COMPARTIR! ---
import android.content.Context
import android.content.Intent
import android.widget.Toast // Para el mensaje de error
// -------------------------------------------

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape // <-- Importante para el botón de compartir
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
// --- ¡NUEVA IMPORTACIÓN PARA EL ICONO DE COMPARTIR! ---
import androidx.compose.material.icons.filled.Share
// ----------------------------------------------------
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.ExperimentalComposeUiApi
// --- ¡NUEVA IMPORTACIÓN PARA EL CONTEXTO! ---
import androidx.compose.ui.platform.LocalContext
// -----------------------------------------
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Importaciones de tu tema, datos y Review
import com.example.gamerstoremvp.ColorAccentBlue
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Product
import com.example.gamerstoremvp.Roboto
import com.example.gamerstoremvp.formatPrice
import com.example.gamerstoremvp.Review

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onAddToCart: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    cart: Map<Product, Int>
) {

    var userRating by remember { mutableStateOf(0) }
    var userReview by remember { mutableStateOf("") }
    var reviews by remember(product.reviews) {
        mutableStateOf(product.reviews)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val currentQuantity = cart[product] ?: 0


    var showRemoveDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // --- ¡NUEVO! OBTENER EL CONTEXTO PARA COMPARTIR ---
    val context = LocalContext.current
    // --------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPrimaryBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // --- CONTENEDOR PARA IMAGEN Y BOTÓN COMPARTIR ---
                Box {
                    Image(
                        painter = painterResource(id = product.imageResId),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // --- ¡NUEVO BOTÓN DE COMPARTIR! ---
                    IconButton(
                        onClick = {
                            shareProduct(context, product)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd) // Lo pone arriba a la derecha
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(ColorPrimaryBackground.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Compartir Producto",
                            tint = ColorTextPrimary
                        )
                    }
                    // ---------------------------------
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product.name,
                        fontFamily = Orbitron,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPrice(product.price),
                        fontFamily = Orbitron,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAccentBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))


                    ProductQuantityControl(
                        quantity = currentQuantity,
                        onIncrease = { onAddToCart(product) },
                        onDecrease = {
                            if (currentQuantity == 1) {
                                showRemoveDialog = true
                            } else {
                                onDecreaseQuantity(product)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )


                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Descripción",
                        fontFamily = Roboto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        fontFamily = Roboto,
                        fontSize = 14.sp,
                        color = ColorTextPrimary
                    )


                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fabricante / Distribuidor",
                        fontFamily = Roboto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.manufacturer,
                        fontFamily = Roboto,
                        fontSize = 14.sp,
                        color = ColorTextPrimary
                    )
                    product.materials?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Materiales",
                            fontFamily = Roboto,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            fontFamily = Roboto,
                            fontSize = 14.sp,
                            color = ColorTextPrimary
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "DEJA TU RESEÑA",
            fontFamily = Orbitron,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorAccentNeon
        )
        Spacer(modifier = Modifier.height(16.dp))
        StarRatingInput(
            rating = userRating,
            onRatingChange = { newUserRating -> userRating = newUserRating }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = userReview,
            onValueChange = { userReview = it },
            label = { Text("Escribe tu opinión...", color = ColorTextSecondary.copy(alpha = 0.7f)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorAccentBlue,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = ColorAccentNeon,
                focusedLabelColor = ColorAccentBlue,
                unfocusedLabelColor = ColorTextSecondary,
                focusedTextColor = ColorTextPrimary,
                unfocusedTextColor = ColorTextPrimary,
                focusedContainerColor = Color.DarkGray.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.1f)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (userRating > 0 && userReview.isNotBlank()) {
                    val newReview = Review(
                        username = "GamerChileno",
                        rating = userRating,
                        comment = userReview
                    )
                    reviews = listOf(newReview) + reviews
                    userRating = 0
                    userReview = ""
                    keyboardController?.hide()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentNeon),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            enabled = userRating > 0 && userReview.isNotBlank()
        ) {
            Text(
                "ENVIAR RESEÑA",
                fontFamily = Orbitron,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ColorPrimaryBackground
            )
        }


        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "RESEÑAS (${reviews.size})",
            fontFamily = Orbitron,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        // --- ¡¡ESTA ES LA LÍNEA QUE FALTABA!! ---
        HorizontalDivider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        if (reviews.isEmpty()) {
            Text(
                text = "Este producto aún no tiene reseñas. ¡Sé el primero!",
                color = ColorTextSecondary,
                fontFamily = Roboto,
                fontSize = 14.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reviews.forEach { review ->
                    ReviewItemCard(review = review)
                }
            }
        }
    }

    // --- (Diálogos - Sin cambios) ---
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Eliminar Producto", fontFamily = Orbitron, color = ColorTextPrimary) },
            text = { Text("¿Estás seguro de que quieres eliminar '${product.name}' del carrito?", fontFamily = Roboto, color = ColorTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDecreaseQuantity(product)
                        showRemoveDialog = false
                        showSuccessDialog = true
                    }
                ) {
                    Text("SÍ, ELIMINAR", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false }
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

// --- ¡NUEVA FUNCIÓN DE LÓGICA PARA COMPARTIR! ---
// (Puedes pegarla al final del archivo)
private fun shareProduct(context: Context, product: Product) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        // Texto que se compartirá
        putExtra(Intent.EXTRA_TEXT, "¡Mira este producto en Level-Up Gamer! \n${product.name} - ${formatPrice(product.price)}\n(Aquí iría un link al producto)")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Compartir ${product.name}")
    try {
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // Manejar error si no hay apps para compartir
        Toast.makeText(context, "No se encontró ninguna app para compartir", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun ProductQuantityControl(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (quantity == 0) {
        Button(
            onClick = onIncrease,
            modifier = modifier,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBlue),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = ColorTextPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "AÑADIR AL CARRITO",
                fontFamily = Orbitron,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ColorTextPrimary
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Quitar uno",
                    tint = ColorTextPrimary
                )
            }

            Text(
                text = "$quantity",
                fontFamily = Orbitron,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorAccentBlue)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir uno",
                    tint = ColorTextPrimary
                )
            }
        }
    }
}


@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..5).forEach { index ->
            IconButton(onClick = { onRatingChange(index) }) {
                Icon(
                    imageVector = if (index <= rating) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = "Valorar $index",
                    tint = if (index <= rating) ColorAccentNeon else ColorTextSecondary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: Review) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.username,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    color = ColorAccentBlue,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    (1..5).forEach { index ->
                        Icon(
                            imageVector = if (index <= review.rating) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = null,
                            tint = ColorAccentNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontFamily = Roboto,
                color = ColorTextPrimary,
                fontSize = 14.sp
            )
        }
    }
}