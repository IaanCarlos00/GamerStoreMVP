package com.example.gamerstoremvp.features.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamerstoremvp.R
import com.example.gamerstoremvp.core.theme.ColorAccentBlue
import com.example.gamerstoremvp.core.theme.ColorAccentNeon
import com.example.gamerstoremvp.core.theme.ColorPrimaryBackground
import com.example.gamerstoremvp.core.theme.ColorTextPrimary
import com.example.gamerstoremvp.core.theme.ColorTextSecondary
import com.example.gamerstoremvp.core.theme.Orbitron
import com.example.gamerstoremvp.core.theme.Roboto
import com.example.gamerstoremvp.core.theme.User
import com.example.gamerstoremvp.core.theme.formatPrice
import com.example.gamerstoremvp.features.auth.AuthTextField
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    onUserUpdate: (User) -> Unit
) {

    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.address) }
    
    // --- ESTADO PARA LA IMAGEN DE PERFIL (URI o Resource ID) ---
    // Si el usuario ya tiene una URI guardada, la cargamos inicialmente
    var selectedImageUri by remember { 
        mutableStateOf<Uri?>(
            if (!user.profileImageUri.isNullOrEmpty()) Uri.parse(user.profileImageUri) else null
        ) 
    }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    var pointsToRedeemStr by remember { mutableStateOf("") }
    var showCouponDialog by remember { mutableStateOf(false) }
    var generatedCoupon by remember { mutableStateOf("") }
    val context = LocalContext.current

    // --- LAUNCHERS PARA CÁMARA Y GALERÍA ---
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Aquí se guarda solo visualmente hasta que se pulse "GUARDAR CAMBIOS"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    // Función para crear un archivo temporal para la foto
    fun createImageFile(): File {
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
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
            text = "MI PERFIL",
            fontFamily = Orbitron,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorAccentNeon
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box {
            // Muestra imagen seleccionada (URI) o por defecto (Resource ID)
            // Prioridad: 1. Imagen seleccionada/guardada URI, 2. Recurso predeterminado
            if (selectedImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(selectedImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de Perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, ColorAccentBlue, CircleShape)
                        .clickable { showImageSourceDialog = true }
                )
            } else {
                Image(
                    painter = painterResource(id = user.profileImageResId ?: R.drawable.profile_pic_default),
                    contentDescription = "Foto de Perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, ColorAccentBlue, CircleShape)
                        .clickable { showImageSourceDialog = true }
                )
            }
            
            // Icono de edición
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar Foto",
                tint = ColorPrimaryBackground,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ColorAccentNeon)
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
                    .clickable { showImageSourceDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        InfoCard(
            title = "MIS PUNTOS LEVEL-UP",
            content = "${user.levelUpPoints} Pts",
            icon = Icons.Default.Star
        )
        InfoCard(
            title = "MI CÓDIGO DE REFERIDO",
            content = user.referralCode.uppercase(),
            icon = Icons.Default.People
        )

        Spacer(modifier = Modifier.height(24.dp))


        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CANJEAR PUNTOS",
                    fontFamily = Orbitron,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorAccentNeon
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingresa cuántos puntos quieres canjear. (1 Punto = 1 CLP)",
                    fontFamily = Roboto,
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))


                AuthTextField(
                    value = pointsToRedeemStr,
                    onValueChange = { pointsToRedeemStr = it.filter { c -> c.isDigit() } },
                    label = "Puntos a canjear (Ej: 5000)",
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))


                Button(
                    onClick = {
                        val pointsToRedeem = pointsToRedeemStr.toIntOrNull() ?: 0

                        if (pointsToRedeem == 0) {
                            Toast.makeText(context, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                        } else if (pointsToRedeem > user.levelUpPoints) {
                            Toast.makeText(context, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show()
                        } else {

                            val newPoints = user.levelUpPoints - pointsToRedeem
                            val updatedUser = user.copy(levelUpPoints = newPoints)
                            onUserUpdate(updatedUser) 

                            generatedCoupon = "LEVELUP$pointsToRedeem"
                            showCouponDialog = true
                            pointsToRedeemStr = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorAccentNeon),
                    enabled = pointsToRedeemStr.isNotBlank()
                ) {
                    Text("GENERAR CUPÓN", fontFamily = Orbitron, fontWeight = FontWeight.Black, color = ColorPrimaryBackground)
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))


        AuthTextField(
            value = user.email,
            onValueChange = {},
            label = "Email (No se puede cambiar)",
            keyboardType = KeyboardType.Email
        )
        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre Completo",
            keyboardType = KeyboardType.Text
        )
        AuthTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Teléfono",
            keyboardType = KeyboardType.Phone
        )
        AuthTextField(
            value = address,
            onValueChange = { address = it },
            label = "Dirección",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                val updatedUser = user.copy(
                    name = name,
                    phone = phone,
                    address = address,
                    // --- ACTUALIZAMOS LA URI DE LA IMAGEN EN EL OBJETO USUARIO ---
                    profileImageUri = selectedImageUri?.toString() 
                    // -------------------------------------------------------------
                )
                onUserUpdate(updatedUser)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBlue),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("GUARDAR CAMBIOS", fontFamily = Orbitron, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("CAMBIAR CONTRASEÑA", fontFamily = Orbitron, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("CERRAR SESIÓN", fontFamily = Orbitron, fontWeight = FontWeight.Black)
        }
    }

    // --- DIALOGO PARA ELEGIR FUENTE DE IMAGEN ---
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Cambiar Foto de Perfil", fontFamily = Orbitron, color = ColorTextPrimary) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Tomar Foto", color = ColorTextPrimary) },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ColorAccentBlue) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            val photoFile = createImageFile()
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                photoFile
                            )
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                    ListItem(
                        headlineContent = { Text("Elegir de Galería", color = ColorTextPrimary) },
                        leadingContent = { Icon(Icons.Default.Image, contentDescription = null, tint = ColorAccentBlue) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("CANCELAR", color = ColorTextSecondary)
                }
            },
            containerColor = Color.DarkGray
        )
    }

    if (showCouponDialog) {
        AlertDialog(
            onDismissRequest = { showCouponDialog = false },
            icon = { Icon(Icons.Filled.Redeem, contentDescription = "Cupón", tint = ColorAccentNeon) },
            title = { Text("¡Cupón Generado!", fontFamily = Orbitron, color = ColorTextPrimary) },
            text = {
                Column {
                    Text("Tu cupón es:", fontFamily = Roboto, color = ColorTextSecondary)
                    Text(
                        text = generatedCoupon,
                        fontFamily = Orbitron,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ColorAccentBlue,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Úsalo en tu carrito de compras para obtener ${
                            formatPrice(
                                generatedCoupon.removePrefix("LEVELUP").toInt().toDouble()
                            )
                        } de descuento.",
                        fontFamily = Roboto,
                        color = ColorTextSecondary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCouponDialog = false }) {
                    Text("ENTENDIDO", color = ColorAccentBlue)
                }
            },
            containerColor = Color.DarkGray
        )
    }
}

// --- FALTABA ESTE COMPOSABLE AL FINAL DEL ARCHIVO ---
@Composable
fun InfoCard(title: String, content: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = ColorAccentNeon, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = Roboto,
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
                Text(
                    text = content,
                    fontFamily = Orbitron,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }
        }
    }
}
