package com.example.gamerstoremvp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color // ¡IMPORTACIÓN NECESARIA para usar Color directamente!

// Importaciones de datos y tema
import com.example.gamerstoremvp.ColorAccentBlue
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Roboto
import kotlinx.coroutines.launch


/**
 * Pantalla de Autenticación (Login/Registro).
 */
@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf("usuario@ejemplo.com") }
    var password by remember { mutableStateOf("123456") }
    var isLogin by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Usamos rememberCoroutineScope para manejar las funciones suspendidas (como delay)
    val coroutineScope = rememberCoroutineScope()

    val buttonText = if (isLogin) "INICIAR SESIÓN" else "REGISTRARSE"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(ColorPrimaryBackground), // Aseguramos el fondo aquí por si acaso
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡BIENVENIDO!",
            fontFamily = Orbitron,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorAccentNeon,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isLogin) "Accede a tu cuenta Level-Up" else "Crea una nueva cuenta",
            fontFamily = Roboto,
            fontSize = 14.sp,
            color = ColorTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = ColorTextSecondary.copy(alpha = 0.7f)) },
            // La importación de KeyboardOptions estaba faltando
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
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
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = ColorTextSecondary.copy(alpha = 0.7f)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
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
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Acción Principal (Login/Registro)
        Button(
            onClick = {
                // CORRECCIÓN: Usa el coroutineScope recordado para llamar a la función suspendida delay
                isLoading = true
                coroutineScope.launch {
                    delay(1000) // Simular un retraso de red
                    isLoading = false
                    onAuthSuccess()
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentNeon),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ColorPrimaryBackground, strokeWidth = 3.dp)
            } else {
                Text(buttonText, fontFamily = Orbitron, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorPrimaryBackground)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opción para cambiar entre Login y Registro
        Text(
            text = if (isLogin) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia Sesión",
            color = ColorAccentBlue,
            fontSize = 14.sp,
            modifier = Modifier.clickable { isLogin = !isLogin }
        )
    }
}