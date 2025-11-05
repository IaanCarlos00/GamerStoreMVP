package com.example.gamerstoremvp


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Importaciones de tu tema
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Roboto

@Composable
fun AboutUsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPrimaryBackground)
            .verticalScroll(rememberScrollState()) // Permite scroll si el texto es largo
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SOBRE NOSOTROS",
            fontFamily = Orbitron,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorAccentNeon,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Puedes añadir aquí un logo si quieres con Image()

        Text(
            text = "¡Bienvenidos a Level-Up Gamer!",
            fontFamily = Roboto,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Somos tu tienda online definitiva para todo lo relacionado con el mundo gamer en Chile. Nacimos hace dos años, en plena pandemia, con la misión de llevar la mejor experiencia y los productos de más alta calidad a todos los rincones del país.",
            fontFamily = Roboto,
            fontSize = 16.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Justify, // Justificar el texto
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "En Level-Up Gamer, no solo vendemos productos; construimos una comunidad. Creemos en el poder de los videojuegos para conectar personas y crear experiencias inolvidables. Por eso, nos esforzamos en ofrecer un servicio al cliente excepcional y apoyar eventos locales que fortalezcan a la comunidad gamer.",
            fontFamily = Roboto,
            fontSize = 16.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Nuestra visión es ser líderes en Chile, innovando constantemente y recompensando a nuestros jugadores más leales. ¡Gracias por ser parte de Level-Up Gamer!",
            fontFamily = Roboto,
            fontSize = 16.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Puedes añadir información de contacto, redes sociales, etc. aquí
    }
}