package com.example.gamerstoremvp.features.orders // SOLO UNA VEZ

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Importación para items en LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // Importación para remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // ¡ASEGÚRATE QUE ESTA IMPORTACIÓN ESTÉ!
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat // Para formatear la fecha
import java.util.* // Para Date y Locale

// Importaciones de tu tema y datos
import com.example.gamerstoremvp.core.theme.ColorAccentBlue
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.Orbitron
import com.example.gamerstoremvp.core.theme.Roboto
import com.example.gamerstoremvp.core.theme.formatPrice // Función para formatear precios
import com.example.gamerstoremvp.core.theme.Order // Importa la data class Order que definimos antes

/**
 * Pantalla que muestra la lista de pedidos del usuario.
 * @param orders La lista de objetos Order a mostrar.
 */
@Composable
fun OrdersScreen(orders: List<Order>) {
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .background(ColorPrimaryBackground) // Fondo negro
            .padding(16.dp), // Padding general
        horizontalAlignment = Alignment.CenterHorizontally // Centra el título
    ) {
        // --- Título de la Pantalla ---
        Text(
            text = "MIS PEDIDOS",
            fontFamily = Orbitron,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorAccentNeon,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Contenido: Lista o Mensaje de Vacío ---
        if (orders.isEmpty()) {
            // Mensaje si no hay pedidos
            Box(
                modifier = Modifier.fillMaxSize(), // Ocupa el espacio restante
                contentAlignment = Alignment.Center // Centra el texto
            ) {
                Text(
                    text = "Aún no tienes pedidos.\n¡Explora nuestro catálogo!",
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            // Lista si hay pedidos
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // Ocupa el espacio restante
                verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre tarjetas de pedido
            ) {
                // Muestra los pedidos más recientes primero
                items(orders.sortedByDescending { it.timestamp }) { order ->
                    OrderItemCard(order = order) // Llama al Composable para cada tarjeta
                }
            }
        }
    }
}

/**
 * Composable que representa una tarjeta individual para un pedido.
 * @param order El objeto Order a mostrar en la tarjeta.
 */
@Composable
fun OrderItemCard(order: Order) {
    // Crea un formateador de fecha (solo se crea una vez gracias a remember)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(12.dp), // Bordes redondeados
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.7f)), // Fondo semitransparente
        modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Padding interno de la tarjeta
            // --- Fila Superior: ID del Pedido y Fecha ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Espacia los elementos
                verticalAlignment = Alignment.CenterVertically // Centra verticalmente
            ) {
                Text(
                    text = "Pedido #${order.id.takeLast(6)}", // Muestra los últimos 6 caracteres del ID
                    fontFamily = Orbitron,
                    fontWeight = FontWeight.Bold,
                    color = ColorAccentNeon,
                    fontSize = 16.sp
                )
                Text(
                    text = dateFormat.format(Date(order.timestamp)), // Muestra la fecha formateada
                    fontFamily = Roboto,
                    color = ColorTextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp)) // Espacio
            Divider(color = ColorTextSecondary.copy(alpha = 0.5f)) // Línea divisora
            Spacer(modifier = Modifier.height(12.dp)) // Espacio

            // --- Lista de Ítems del Pedido ---
            order.items.forEach { item -> // Itera sobre cada OrderItem en el pedido
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nombre y cantidad del producto
                    Text(
                        text = "${item.quantity}x ${item.productName}",
                        fontFamily = Roboto,
                        color = ColorTextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp) // Ocupa espacio flexible
                    )
                    // Precio total para ese ítem (cantidad * precio unitario)
                    Text(
                        text = formatPrice(item.pricePerUnit * item.quantity),
                        fontFamily = Roboto,
                        color = ColorTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // Espacio
            Divider(color = ColorTextSecondary.copy(alpha = 0.5f)) // Línea divisora
            Spacer(modifier = Modifier.height(8.dp)) // Espacio

            // --- Fila Inferior: Total del Pedido ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Alinea el total a la derecha
            ) {
                Text(
                    text = "Total: ${formatPrice(order.totalAmount)}",
                    fontFamily = Orbitron,
                    fontWeight = FontWeight.Bold,
                    color = ColorAccentBlue,
                    fontSize = 16.sp
                )
            }
        }
    }
}