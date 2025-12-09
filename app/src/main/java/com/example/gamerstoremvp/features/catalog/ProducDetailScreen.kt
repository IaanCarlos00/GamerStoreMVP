package com.example.gamerstoremvp.features.catalog

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamerstoremvp.core.data.AppDatabase
import com.example.gamerstoremvp.core.data.ReviewRepository
import com.example.gamerstoremvp.core.theme.*
import com.example.gamerstoremvp.models.Product

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProductDetailScreen(
    productCode: String,
    onAddToCart: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    cart: Map<Int, Int>
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val reviewRepository = ReviewRepository(database.reviewDao())
    val viewModel: ProductDetailViewModel = viewModel(
        factory = ProductDetailViewModelFactory(reviewRepository, productCode)
    )

    val productState by viewModel.product.collectAsStateWithLifecycle()

    when (val state = productState) {
        is ProductDetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(ColorPrimaryBackground)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        is ProductDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(ColorPrimaryBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is ProductDetailUiState.Success -> {
            val reviews by viewModel.reviews.collectAsStateWithLifecycle()
            ProductDetailContent(
                product = state.product,
                reviews = reviews,
                onAddToCart = onAddToCart,
                onDecreaseQuantity = onDecreaseQuantity,
                cart = cart,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ProductDetailContent(
    product: Product,
    reviews: List<Review>,
    onAddToCart: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    cart: Map<Int, Int>,
    viewModel: ProductDetailViewModel
) {
    val context = LocalContext.current
    var userRating by remember { mutableIntStateOf(0) }
    var userReview by remember { mutableStateOf("") }
    var showReviewForm by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentQuantity = cart[product.id] ?: 0

    var showRemoveDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { shareProduct(context, product) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Calificación",
                            tint = ColorAccentNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating} (${product.reviews} reseñas)",
                            fontFamily = Roboto,
                            fontSize = 14.sp,
                            color = ColorTextSecondary
                        )
                    }
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
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showReviewForm = !showReviewForm },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorAccentNeon),
            border = BorderStroke(1.dp, ColorAccentNeon)
        ) {
            Text(
                text = if (showReviewForm) "OCULTAR FORMULARIO" else "DEJA TU RESEÑA",
                fontFamily = Orbitron,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = showReviewForm,
            enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)) + shrinkVertically(animationSpec = tween(500))
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
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
                            viewModel.addReview(
                                username = "GamerChileno",
                                rating = userRating,
                                comment = userReview
                            )
                            userRating = 0
                            userReview = ""
                            keyboardController?.hide()
                            showReviewForm = false
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
            }
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
        HorizontalDivider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(durationMillis = 500)
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reviews.isEmpty()) {
                Text(
                    text = "Este producto aún no tiene reseñas. ¡Sé el primero!",
                    color = ColorTextSecondary,
                    fontFamily = Roboto,
                    fontSize = 14.sp
                )
            } else {
                reviews.forEachIndexed { index, review ->
                    AnimatedReviewItemCard(review = review, index = index)
                }
            }
        }
    }

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

private fun shareProduct(context: Context, product: Product) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, """¡Mira este producto en Level-Up Gamer!
${product.name} - ${formatPrice(product.price)}
(Aquí iría un link al producto)""")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Compartir ${product.name}")
    try {
        context.startActivity(shareIntent)
    } catch (_: Exception) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedReviewItemCard(review: Review, index: Int) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = index * 100)) +
                slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(durationMillis = 500, delayMillis = index * 100)),
    ) {
        ReviewItemCard(review = review)
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
