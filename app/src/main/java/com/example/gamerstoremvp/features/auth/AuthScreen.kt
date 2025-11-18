package com.example.gamerstoremvp.features.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamerstoremvp.R
import com.example.gamerstoremvp.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

// Importaciones de tu tema
import com.example.gamerstoremvp.core.theme.ColorAccentBlue
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.Orbitron
import com.example.gamerstoremvp.core.theme.Roboto
import com.example.gamerstoremvp.core.theme.User

@Composable
fun AuthScreen(
    userViewModel: UserViewModel, // Recibe el ViewModel
    onAuthSuccess: (User) -> Unit
) {
    // Estados para los campos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") } // <-- ¡¡NUEVO CAMPO!!

    var isLogin by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val buttonText = if (isLogin) "INICIAR SESIÓN" else "REGISTRARSE"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(ColorPrimaryBackground)
            .verticalScroll(rememberScrollState()),
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

        // --- Muestra campos adicionales solo si NO es Login ---
        if (!isLogin) {
            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre Completo",
                keyboardType = KeyboardType.Text
            )
            AuthTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Número de Teléfono",
                keyboardType = KeyboardType.Phone
            )
            AuthTextField(
                value = address,
                onValueChange = { address = it },
                label = "Dirección",
                keyboardType = KeyboardType.Text
            )

            AuthTextField(
                value = dob,
                onValueChange = { newValue ->
                    dob = newValue.filter { it.isDigit() }.take(8)
                },
                label = "Fecha de Nacimiento (DD/MM/AAAA)",
                keyboardType = KeyboardType.Number,
                visualTransformation = DateVisualTransformation()
            )

            // --- ¡¡NUEVO CAMPO DE REFERIDO!! ---
            AuthTextField(
                value = referralCode,
                onValueChange = { referralCode = it },
                label = "Código de Referido (Opcional)",
                keyboardType = KeyboardType.Text
            )
        }
        // ---------------------------------------------

        // Campos comunes (Email y Contraseña)
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Botón Principal (Login o Registro) ---
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    delay(1000)
                    isLoading = false

                    if (isLogin) {
                        // --- LÓGICA DE LOGIN (Sin cambios) ---
                        val foundUser = userViewModel.allUsers.find { it.email.equals(email, ignoreCase = true) && it.password == password }
                        if (foundUser != null) {
                            onAuthSuccess(foundUser)
                        } else {
                            Toast.makeText(context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // --- LÓGICA DE REGISTRO (Actualizada con Referido) ---

                        // 1. Validaciones de campos vacíos
                        if (name.isBlank() || phone.isBlank() || address.isBlank() || email.isBlank() || password.isBlank() || dob.isBlank()) {
                            Toast.makeText(context, "Por favor, rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show()

                            // 2. Validación de formato de fecha
                        } else if (dob.length != 8) {
                            Toast.makeText(context, "Fecha de nacimiento incompleta", Toast.LENGTH_SHORT).show()

                        } else {

                            var age: Int

                            try {
                                // 3. Calcular la edad
                                val day = dob.substring(0, 2).toInt()
                                val month = dob.substring(2, 4).toInt()
                                val year = dob.substring(4, 8).toInt()
                                val dobCalendar = Calendar.getInstance()
                                dobCalendar.set(year, month - 1, day)
                                val todayCalendar = Calendar.getInstance()
                                age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
                                if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                                    age--
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Fecha de nacimiento inválida", Toast.LENGTH_SHORT).show()
                                age = 0
                            }

                            // 4. Validación de Edad
                            if (age < 18) {
                                Toast.makeText(context, "No se puede registrar por ser menor de edad", Toast.LENGTH_SHORT).show()

                            } else {
                                // --- ¡NUEVA LÓGICA DE REFERIDO! ---

                                var referringUser: User? = null
                                var bonusPoints = 0 // Puntos para el nuevo usuario

                                if (referralCode.isNotBlank()) {
                                    // Busca al usuario que posee el código
                                    referringUser = userViewModel.allUsers.find {
                                        it.referralCode.equals(referralCode, ignoreCase = true)
                                    }

                                    if (referringUser == null) {
                                        Toast.makeText(context, "Código de referido no válido. Se registrará sin bonos.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "¡Código válido! Recibirás 1000 puntos extra.", Toast.LENGTH_SHORT).show()
                                        bonusPoints = 1000 // Puntos para el nuevo usuario (según PDF)
                                    }
                                }
                                // --------------------------------------

                                // 5. Comprueba si el email ya existe
                                if (userViewModel.allUsers.any { it.email.equals(email, ignoreCase = true) }) {
                                    Toast.makeText(context, "Este email ya está registrado", Toast.LENGTH_SHORT).show()

                                    // 6. (ÉXITO) Crear nuevo usuario
                                } else {
                                    val newUser = User(
                                        name = name,
                                        email = email,
                                        password = password,
                                        phone = phone,
                                        address = address,
                                        profileImageResId = R.drawable.profile_pic_default,
                                        levelUpPoints = 1000 + bonusPoints // 1000 base + 1000 de bono si aplica
                                    )

                                    // El ViewModel se encarga de dar puntos al que refirió
                                    userViewModel.registerUser(newUser, referringUser)

                                    onAuthSuccess(newUser) // Inicia la sesión
                                }
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
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

        Text(
            text = if (isLogin) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia Sesión",
            color = ColorAccentBlue,
            fontSize = 14.sp,
            modifier = Modifier.clickable { isLogin = !isLogin }
        )
    }
}

// --- Composable reutilizable para los campos de texto (Sin cambios) ---
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ColorTextSecondary.copy(alpha = 0.7f)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else visualTransformation,
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true
    )
}

// --- Clase para Formato de Fecha (Sin cambios) ---
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text

        val annotatedString = buildAnnotatedString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 1 || i == 3) {
                    append("/")
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }

        return TransformedText(annotatedString, offsetMapping)
    }
}