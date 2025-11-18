package com.example.gamerstoremvp.features.catalog

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Asegúrate de tener esta importación
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
// Importaciones de datos y tema
import com.example.gamerstoremvp.core.theme.ColorAccentBlue
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.Orbitron
import com.example.gamerstoremvp.core.theme.Product
import com.example.gamerstoremvp.core.theme.Roboto
import com.example.gamerstoremvp.core.theme.formatPrice
import com.example.gamerstoremvp.core.theme.mockProducts



@Composable
fun CatalogScreen(
    products: List<Product>,
    onAddToCart: (Product) -> Unit,
    onProductClick: (Product) -> Unit
) {

    var selectedCategory by remember { mutableStateOf("Todos") }

    var searchQuery by remember { mutableStateOf("") }


    val filteredProducts = remember(selectedCategory, searchQuery, products) {
        products
            .filter { product ->

                if (selectedCategory == "Todos") true else product.category == selectedCategory
            }
            .filter { product ->

                product.name.contains(searchQuery, ignoreCase = true)
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(ColorPrimaryBackground),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                WelcomeBanner(
                    onTitleClick = {
                        selectedCategory = "Todos"
                        searchQuery = ""
                    }
                )

                SearchBarAndFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { newQuery -> searchQuery = newQuery },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { newCategory -> selectedCategory = newCategory }
                )
            }
        }


        items(filteredProducts) { product ->

            ProductGridCard(
                product = product,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick
            )
        }
    }
}

@Composable
fun WelcomeBanner(onTitleClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray.copy(alpha = 0.5f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BIENVENIDO A Level-Up Gaming Store",
                fontFamily = Orbitron,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAccentNeon,
                modifier = Modifier.clickable { onTitleClick() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu tienda gamer de confianza. Encuentra los mejores productos para potenciar tu experiencia de juego.",
                fontFamily = Roboto,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = ColorTextSecondary
            )
        }
    }
}


@Composable
fun SearchBarAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar productos...", color = ColorTextSecondary.copy(alpha = 0.7f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = ColorTextSecondary) },
            singleLine = true,
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("Todos") + mockProducts.map { it.category }.distinct()
            items(categories) { category ->
                Button(
                    onClick = { onCategorySelected(category) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(

                        containerColor = if (category == selectedCategory) ColorAccentBlue else Color.DarkGray,
                        contentColor = ColorTextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(category, fontFamily = Roboto, fontSize = 12.sp)
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun ProductGridCard(
    product: Product,
    onAddToCart: (Product) -> Unit,
    onProductClick: (Product) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),

        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = product.imageResId),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {

                Text(
                    text = product.category,
                    fontFamily = Roboto,
                    fontSize = 10.sp,
                    color = ColorTextSecondary
                )
                Text(
                    text = product.name,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = ColorAccentNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.1f", product.rating)} (${product.reviewCount})",
                        fontFamily = Roboto,
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(product.price),
                        fontFamily = Orbitron,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorAccentBlue
                    )
                    IconButton(
                        onClick = { onAddToCart(product) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .size(36.dp)
                            .background(ColorAccentBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Añadir al Carrito",
                            tint = ColorTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}