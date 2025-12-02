package com.example.gamerstoremvp.features.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
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
import com.example.gamerstoremvp.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

// --- Visual Transformations (Tarjeta y Fecha) ---
class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
        val annotatedString = buildAnnotatedString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i % 4 == 3 && i != 15) append(" ")
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

class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        val annotatedString = buildAnnotatedString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 1) append("/")
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

@Composable
fun CheckoutScreen(
    totalAmount: Int,
    onPaymentSuccess: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Estados de error visual
    var isCardNumberError by remember { mutableStateOf(false) }
    var isExpiryDateError by remember { mutableStateOf(false) }
    var isCvcError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun validateFields(): Boolean {
        isNameError = cardHolderName.isBlank()
        isCardNumberError = cardNumber.length != 16
        isCvcError = cvc.length != 3

        // Validación Fecha
        isExpiryDateError = true
        if (expiryDate.length == 4) {
            val monthStr = expiryDate.substring(0, 2)
            val yearStr = expiryDate.substring(2, 4)
            try {
                val month = monthStr.toInt()
                val year = yearStr.toInt() + 2000
                val currentCalendar = Calendar.getInstance()
                val currentYear = currentCalendar.get(Calendar.YEAR)
                val currentMonth = currentCalendar.get(Calendar.MONTH) + 1
                if (month in 1..12) {
                    if (year > currentYear || (year == currentYear && month >= currentMonth)) {
                        isExpiryDateError = false
                    }
                }
            } catch (e: Exception) { isExpiryDateError = true }
        }
        return !isNameError && !isCardNumberError && !isExpiryDateError && !isCvcError
    }

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
        Text("FORMULARIO DE PAGO", fontFamily = Orbitron, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorAccentNeon)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Total a Pagar: ${formatPrice(totalAmount.toDouble())}", fontFamily = Roboto, fontSize = 18.sp, color = ColorTextPrimary, modifier = Modifier.padding(bottom = 24.dp))

        CheckoutTextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it; isNameError = it.isBlank() },
            label = "Nombre en la Tarjeta",
            keyboardType = KeyboardType.Text,
            isError = isNameError
        )
        CheckoutTextField(
            value = cardNumber,
            onValueChange = {
                val digits = it.filter { c -> c.isDigit() }
                if (digits.length <= 16) cardNumber = digits
                isCardNumberError = cardNumber.length != 16
            },
            label = "Número de Tarjeta",
            keyboardType = KeyboardType.Number,
            visualTransformation = CardNumberVisualTransformation(),
            leadingIcon = { Icon(Icons.Filled.CreditCard, "Tarjeta") },
            isError = isCardNumberError
        )

        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) {
                CheckoutTextField(
                    value = expiryDate,
                    onValueChange = {
                        val digits = it.filter { c -> c.isDigit() }
                        if (digits.length <= 4) expiryDate = digits
                        isExpiryDateError = expiryDate.length != 4
                    },
                    label = "MM/AA",
                    keyboardType = KeyboardType.Number,
                    visualTransformation = ExpiryDateVisualTransformation(),
                    leadingIcon = { Icon(Icons.Filled.CalendarMonth, "Fecha") },
                    isError = isExpiryDateError
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(Modifier.weight(1f)) {
                CheckoutTextField(
                    value = cvc,
                    onValueChange = {
                        val digits = it.filter { c -> c.isDigit() }
                        if (digits.length <= 3) cvc = digits
                        isCvcError = cvc.length != 3
                    },
                    label = "CVC",
                    keyboardType = KeyboardType.Number,
                    leadingIcon = { Icon(Icons.Filled.Lock, "CVC") },
                    isError = isCvcError
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (validateFields()) {
                    isLoading = true
                    coroutineScope.launch {
                        delay(2000)
                        isLoading = false
                        onPaymentSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBlue),
            enabled = isPayButtonEnabled
        ) {
            if (isLoading) CircularProgressIndicator(color = ColorTextPrimary, strokeWidth = 3.dp)
            else Text("PAGAR AHORA", fontFamily = Orbitron, fontWeight = FontWeight.Black, color = ColorTextPrimary)
        }
    }
}

@Composable
fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isError) Color.Red else ColorTextSecondary.copy(alpha = 0.7f)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color.Red else ColorAccentBlue,
            unfocusedBorderColor = if (isError) Color.Red.copy(alpha=0.5f) else Color.DarkGray,
            cursorColor = ColorAccentNeon,
            focusedLabelColor = if (isError) Color.Red else ColorAccentBlue,
            unfocusedLabelColor = if (isError) Color.Red.copy(alpha=0.7f) else ColorTextSecondary,
            focusedTextColor = ColorTextPrimary,
            unfocusedTextColor = ColorTextPrimary,
            focusedContainerColor = Color.DarkGray.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.1f),
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red,
            errorLeadingIconColor = Color.Red
        ),
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true
    )
}
