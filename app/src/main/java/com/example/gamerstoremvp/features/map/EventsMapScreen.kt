package com.example.gamerstoremvp.features.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamerstoremvp.core.theme.ColorAccentBlue
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.Orbitron

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

data class GameEvent(
    val name: String,
    val position: LatLng,
    val points: Int
)

@Composable
fun EventsMapScreen() {

    val santiago = LatLng(-33.4489, -70.6693)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santiago, 5f)
    }


    val eventsList = remember {
        mutableStateListOf(
            GameEvent("Torneo Valorant Santiago", LatLng(-33.4489, -70.6693), 500),
            GameEvent("Feria Gamer Concepción", LatLng(-36.8201, -73.0444), 300),
            GameEvent("Lanzamiento Viña del Mar", LatLng(-33.0246, -71.5518), 200),
            GameEvent("Meetup La Serena", LatLng(-29.9027, -71.2519), 150)
        )
    }


    var showDialog by remember { mutableStateOf(false) }
    var newEventPosition by remember { mutableStateOf<LatLng?>(null) }
    var newEventName by remember { mutableStateOf("") }
    var newEventPoints by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPrimaryBackground)
        ) {
            Text(
                text = "MAPA DE EVENTOS",
                fontFamily = Orbitron,
                fontSize = 24.sp,
                color = ColorTextPrimary,
                modifier = Modifier.padding(16.dp)
            )


            Text(
                text = "Mantén presionado en el mapa para crear un evento.",
                color = ColorTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true),

                onMapLongClick = { latLng ->
                    newEventPosition = latLng
                    showDialog = true
                }
            ) {

                eventsList.forEach { event ->
                    Marker(
                        state = MarkerState(position = event.position),
                        title = event.name,
                        snippet = "Gana ${event.points} Puntos LevelUp",
                        icon = null
                    )
                }
            }
        }


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color.DarkGray,
                title = {
                    Text("Nuevo Evento Gamer", fontFamily = Orbitron, color = ColorAccentNeon)
                },
                text = {
                    Column {
                        Text("Ingresa los datos del evento:", color = ColorTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))


                        OutlinedTextField(
                            value = newEventName,
                            onValueChange = { newEventName = it },
                            label = { Text("Nombre del Evento", color = ColorTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorTextPrimary,
                                unfocusedTextColor = ColorTextPrimary,
                                focusedBorderColor = ColorAccentBlue,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))


                        OutlinedTextField(
                            value = newEventPoints,
                            onValueChange = { newEventPoints = it.filter { char -> char.isDigit() } },
                            label = { Text("Puntos a ganar", color = ColorTextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorTextPrimary,
                                unfocusedTextColor = ColorTextPrimary,
                                focusedBorderColor = ColorAccentBlue,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newEventName.isNotBlank() && newEventPoints.isNotBlank() && newEventPosition != null) {

                                val newEvent = GameEvent(
                                    name = newEventName,
                                    position = newEventPosition!!,
                                    points = newEventPoints.toInt()
                                )

                                eventsList.add(newEvent)


                                newEventName = ""
                                newEventPoints = ""
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentNeon)
                    ) {
                        Text("CREAR", color = ColorPrimaryBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("CANCELAR", color = ColorTextSecondary)
                    }
                }
            )
        }
    }
}