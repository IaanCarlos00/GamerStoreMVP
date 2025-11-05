package com.example.gamerstoremvp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons // Importar iconos
import androidx.compose.material.icons.filled.CalendarMonth // Icono calendario
import androidx.compose.material.icons.filled.CreditCard // Icono tarjeta
import androidx.compose.material.icons.filled.Lock // Icono candado (CVC)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamerstoremvp.ColorAccentBlue
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Roboto
import com.example.gamerstoremvp.formatPrice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar // Para validación de fecha

// --- Visual Transformations para formato automático ---

// Formato Tarjeta: XXXX XXXX XXXX XXXX
class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
        val annotatedString = buildAnnotatedString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i % 4 == 3 && i != 15) {
                    append(" ")
                }
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }
        return TransformedText(annotatedString, offsetMapping)
    }
}

// Formato Fecha: MM/YY
class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        val annotatedString = buildAnnotatedString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 1) { // Poner / después del segundo dígito (mes)
                    append("/")
                }
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(annotatedString, offsetMapping)
    }
}

// --- Fin Visual Transformations ---


@Composable
fun CheckoutScreen(
    totalAmount: Int,
    onPaymentSuccess: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") } // Guardará MMYY
    var cvc by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Estados para validación de errores
    var isCardNumberError by remember { mutableStateOf(false) }
    var isExpiryDateError by remember { mutableStateOf(false) }
    var isCvcError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Función para validar TODOS los campos
    fun validateFields(): Boolean {
        // Validación Nombre
        isNameError = cardHolderName.isBlank()

        // Validación Número Tarjeta (16 dígitos)
        isCardNumberError = cardNumber.length != 16

        // Validación CVC (3 dígitos)
        isCvcError = cvc.length != 3

        // Validación Fecha Expiración
        isExpiryDateError = true // Asumir error inicialmente
        if (expiryDate.length == 4) {
            val monthStr = expiryDate.substring(0, 2)
            val yearStr = expiryDate.substring(2, 4)
            try {
                val month = monthStr.toInt()
                val year = yearStr.toInt() + 2000 // Asumir años 20xx

                val currentCalendar = Calendar.getInstance()
                val currentYear = currentCalendar.get(Calendar.YEAR)
                val currentMonth = currentCalendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-indexado

                if (month in 1..12) { // Mes válido
                    if (year > currentYear || (year == currentYear && month >= currentMonth)) {
                        // Fecha válida (no expirada)
                        isExpiryDateError = false
                    }
                }
            } catch (e: NumberFormatException) {
                // Si no se puede convertir a número, es error
                isExpiryDateError = true
            }
        }

        // Devuelve true si NINGÚN campo tiene error
        return !isNameError && !isCardNumberError && !isExpiryDateError && !isCvcError
    }

    // Calcular si el botón Pagar debe estar habilitado
    val isPayButtonEnabled by remember(cardNumber, expiryDate, cvc, cardHolderName, isLoading) {
        derivedStateOf {
            cardNumber.length == 16 && expiryDate.length == 4 && cvc.length == 3 && cardHolderName.isNotBlank() && !isLoading
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPrimaryBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FORMULARIO DE PAGO", /*...*/
            fontFamily = Orbitron, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorAccentNeon
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Total a Pagar: ${formatPrice(totalAmount)}", /*...*/
            fontFamily = Roboto, fontSize = 18.sp, color = ColorTextPrimary, modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Campos del Formulario ---
        CheckoutTextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it; isNameError = it.isBlank() }, // Validar al cambiar
            label = "Nombre en la Tarjeta",
            keyboardType = KeyboardType.Text,
            isError = isNameError // Pasar estado de error
        )

        CheckoutTextField(
            value = cardNumber,
            onValueChange = {
                // Solo permitir dígitos y limitar longitud
                val digitsOnly = it.filter { char -> char.isDigit() }
                if (digitsOnly.length <= 16) cardNumber = digitsOnly
                isCardNumberError = cardNumber.length != 16 // Validar al cambiar
            },
            label = "Número de Tarjeta",
            keyboardType = KeyboardType.Number,
            visualTransformation = CardNumberVisualTransformation(), // Aplicar formato
            leadingIcon = { Icon(Icons.Filled.CreditCard, contentDescription = "Número Tarjeta") }, // Icono
            isError = isCardNumberError // Pasar estado de error
        )

        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) {
                CheckoutTextField(
                    value = expiryDate,
                    onValueChange = {
                        val digitsOnly = it.filter { char -> char.isDigit() }
                        if (digitsOnly.length <= 4) expiryDate = digitsOnly
                        // Validación simple de longitud al cambiar
                        isExpiryDateError = expiryDate.length != 4
                        // Validación completa se hará al presionar Pagar
                    },
                    label = "MM/AA",
                    keyboardType = KeyboardType.Number,
                    visualTransformation = ExpiryDateVisualTransformation(), // Aplicar formato
                    leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Fecha Expiración") }, // Icono
                    isError = isExpiryDateError // Pasar estado de error
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(Modifier.weight(1f)) {
                CheckoutTextField(
                    value = cvc,
                    onValueChange = {
                        val digitsOnly = it.filter { char -> char.isDigit() }
                        if (digitsOnly.length <= 3) cvc = digitsOnly
                        isCvcError = cvc.length != 3 // Validar al cambiar
                    },
                    label = "CVC",
                    keyboardType = KeyboardType.Number,
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "CVC") }, // Icono
                    isError = isCvcError // Pasar estado de error
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Botón Confirmar Pago ---
        Button(
            onClick = {
                // Primero, validar todos los campos
                if (validateFields()) {
                    // Si son válidos, simular pago
                    isLoading = true
                    coroutineScope.launch {
                        delay(2000) // Simular 2 segundos de procesamiento
                        isLoading = false
                        onPaymentSuccess() // Llama a la función de éxito
                    }
                }
                // Si no son válidos, los campos ya muestran el error visualmente
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBlue),
            enabled = isPayButtonEnabled // Habilitar/deshabilitar según validación y carga
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ColorTextPrimary, strokeWidth = 3.dp)
            } else {
                Text(
                    "PAGAR AHORA", /*...*/
                    fontFamily = Orbitron, fontWeight = FontWeight.Black, color = ColorTextPrimary
                )
            }
        }
    }
}

// Composable reutilizable para los campos (MODIFICADO para aceptar isError y icono)
@Composable
fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null, // Icono opcional
    isError: Boolean = false // Estado de error
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isError) Color.Red else ColorTextSecondary.copy(alpha = 0.7f)) }, // Cambia color label si hay error
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon, // Mostrar icono si se proporciona
        isError = isError, // Indica visualmente el error en el campo
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color.Red else ColorAccentBlue, // Borde rojo si hay error
            unfocusedBorderColor = if (isError) Color.Red.copy(alpha=0.5f) else Color.DarkGray, // Borde rojo tenue si hay error
            cursorColor = ColorAccentNeon,
            focusedLabelColor = if (isError) Color.Red else ColorAccentBlue,
            unfocusedLabelColor = if (isError) Color.Red.copy(alpha=0.7f) else ColorTextSecondary,
            focusedTextColor = ColorTextPrimary,
            unfocusedTextColor = ColorTextPrimary,
            focusedContainerColor = Color.DarkGray.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.1f),
            errorBorderColor = Color.Red, // Color explícito para borde de error
            errorLabelColor = Color.Red, // Color explícito para label de error
            errorLeadingIconColor = Color.Red // Color explícito para icono de error
        ),
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true
    )
}