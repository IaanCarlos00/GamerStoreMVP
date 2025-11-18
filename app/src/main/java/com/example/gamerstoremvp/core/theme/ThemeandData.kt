package com.example.gamerstoremvp.core.theme

// ¡Ya no importamos mutableStateListOf!
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
// 🚨 IMPORTANTE: Necesitas esta importación para que R.drawable funcione.
import com.example.gamerstoremvp.R

// -------------------------------------------------------------------------------------
// 1. CONSTANTES DE COLOR
// -------------------------------------------------------------------------------------

val ColorPrimaryBackground = Color(0xFF000000) // Negro
val ColorAccentBlue = Color(0xFF1E90FF)     // Azul Eléctrico
val ColorAccentNeon = Color(0xFF39FF14)     // Verde Neón
val ColorTextPrimary = Color(0xFFFFFFFF)    // Blanco
val ColorTextSecondary = Color(0xFFD3D3D3)  // Gris Claro

// -------------------------------------------------------------------------------------
// 2. TIPOGRAFÍA
// -------------------------------------------------------------------------------------

val Roboto = FontFamily.Default // Fuente estándar
val Orbitron = FontFamily.SansSerif // Fuente estilo "gamer" para títulos

// -------------------------------------------------------------------------------------
// 3. MODELOS DE DATOS (ACTUALIZADO CON PEDIDOS)
// -------------------------------------------------------------------------------------

/**
 * Representa una reseña de usuario.
 */
data class Review(
    val username: String,
    val rating: Int,
    val comment: String
)

/**
 * Representa un producto en el catálogo.
 */
data class Product(
    val code: String,
    val category: String,
    val name: String,
    val price: Int,
    val description: String,
    val rating: Float,
    val reviewCount: Int,
    val imageResId: Int,
    val reviews: List<Review> = emptyList(),
    val manufacturer: String, // Fabricante o Distribuidor
    val materials: String? = null // Materiales (opcional)
)

/**
 * Representa un usuario.
 */
data class User(
    val id: String = "u${System.currentTimeMillis()}", // ID único
    var name: String,
    var email: String,
    var password: String,
    var phone: String,
    var address: String,
    var profileImageResId: Int? = null,

    // --- ¡NUEVOS CAMPOS DE GAMIFICACIÓN! ---
    var levelUpPoints: Int = 0, // Puntos del usuario
    val referralCode: String = UUID.randomUUID().toString().take(8) // Código único de 8 dígitos
)

// --- ¡¡¡NUEVO!!! DATA CLASSES PARA PEDIDOS ---
/**
 * Representa un ítem específico dentro de un pedido.
 */
data class OrderItem(
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Int // Precio al momento de la compra
)

/**
 * Representa un pedido completo realizado por un usuario.
 */
data class Order(
    val id: String = "ord_${System.currentTimeMillis()}", // ID único del pedido
    val timestamp: Long = System.currentTimeMillis(), // Fecha/Hora de la compra (en milisegundos)
    val items: List<OrderItem>, // Lista de productos comprados
    val totalAmount: Int, // Monto total del pedido
    val userId: String // ID del usuario que realizó el pedido
)
// ---------------------------------------------

/**
 * Define las diferentes pantallas/rutas de la aplicación.
 */
enum class Screen {
    AUTH,
    CATALOG,
    CART,
    PRODUCT_DETAIL,
    PROFILE,
    CHECKOUT,
    ORDERS,
    ABOUT_US // Asegúrate que todas estas estén
}

// --- ¡¡mockUsers ELIMINADO!! ---
// El UserViewModel ahora se encarga de esto.


// --- Reseñas de ejemplo ---
// (Asegúrate de tener todas tus variables mockReviews aquí)
val mockReviewsCatan = listOf(
    Review("GamerPro", 5, "Un clásico, nunca falla. Horas de diversión aseguradas con amigos."),
    Review("EstrategiaMaster", 4, "Muy bueno, aunque depende mucho de la suerte con los dados. Recomendado.")
)
val mockReviewsHyperX = listOf(
    Review("Audiofilo88", 5, "El mejor headset que he tenido por este precio. El 7.1 es increíble y son súper cómodos."),
    Review("StreamerNovato", 4, "Buen sonido, la gente me escucha claro. El cable es un poco largo, pero se arregla.")
)
val mockReviewsPS5 = listOf(
    Review("SonyFan", 5, "¡La mejor consola! Los tiempos de carga no existen y el DualSense es una revolución."),
    Review("CasualPlayer", 5, "Silenciosa y potente. Vale la pena.")
)
val mockReviewsSilla = listOf(
    Review("DeveloperCansado", 5, "Mi espalda me lo agradece. Paso 8 horas sentado y es como estar en una nube. Vale cada peso."),
    Review("DesignGirl", 4, "Es cómoda y muy bonita, pero un poco grande para mi setup.")
)
val mockReviewsMouse = listOf(
    Review("ShooterKing", 5, "Precisión pura. El mejor mouse que he tenido para shooters. El sensor es perfecto."),
)
val mockReviewsCarcassonne = listOf(
    Review("MeepleFan", 5, "Fácil de aprender, muy rejugable. Ideal para 2 personas.")
)
val mockReviewsXbox = listOf(
    Review("PCGamer", 5, "El mejor control para PC, sin duda. La textura es genial.")
)
val mockReviewsPcAsus = listOf(
    Review("RGB_Fanatic", 5, "Una bestia. Corre todo en Ultra a 4K. El diseño es brutal.")
)


// --- LISTA DE PRODUCTOS COMPLETA ---
// (Asegúrate de que la lista mockProducts esté completa con todos los productos)
val mockProducts = listOf(
    Product("JM001", "Juegos de Mesa", "Catan", 29990, "Un clásico juego de estrategia donde los jugadores compiten por colonizar y expandirse en la isla de Catan. Ideal para 3-4 jugadores y perfecto para noches de juego en familia o con amigos.", 4.8f, 142, R.drawable.catan, mockReviewsCatan, "Devir (Distribuidor Oficial)", "Cartón de alta densidad, madera"),
    Product("AC002", "Accesorios", "Auriculares Gamer HyperX Cloud II", 79990, "Proporcionan un sonido envolvente de calidad con un micrófono desmontable y almohadillas de espuma viscoelástica para mayor comodidad durante largas sesiones de juego.", 4.9f, 567, R.drawable.hyperx, mockReviewsHyperX, "HyperX (Fabricante)", "Aluminio, plástico, espuma viscoelástica"),
    Product("CO001", "Consolas", "PlayStation 5", 549990, "La consola de última generación de Sony, que ofrece gráficos impresionantes y tiempos de carga ultrarrápidos para una experiencia de juego inmersiva.", 4.9f, 892, R.drawable.ps5digital, mockReviewsPS5, "Sony Interactive Entertainment (Fabricante)", "Plástico, metales, silicio"),
    Product("SG001", "Sillas Gamers", "Silla Gamer Secretlab Titan", 349990, "Diseñada para el máximo confort, esta silla ofrece un soporte ergonómico y personalización ajustable para sesiones de juego prolongadas.", 4.8f, 456, R.drawable.sillagamer, mockReviewsSilla, "Secretlab (Fabricante)", "Cuero sintético PU, espuma curada en frío, metal"),
    Product("MS001", "Mouse", "Mouse Gamer Logitech G502 HERO", 49990, "Con sensor de alta precisión y botones personalizables, este mouse es ideal para gamers que buscan un control preciso y personalización.", 4.7f, 678, R.drawable.hero, mockReviewsMouse, "Logitech (Fabricante)", "Plástico, componentes electrónicos"),
    Product("JM002", "Juegos de Mesa", "Carcassonne", 24990, "Un juego de colocación de fichas donde los jugadores construyen el paisaje alrededor de la fortaleza medieval de Carcassonne. Ideal para 2-5 jugadores y fácil de aprender.", 4.7f, 210, R.drawable.carcassonne, mockReviewsCarcassonne, "Devir (Distribuidor Oficial)", "Cartón de alta densidad"),
    Product("AC001", "Accesorios", "Controlador Inalámbrico Xbox Series X", 59990, "Ofrece una experiencia de juego cómoda con botones mapeables y una respuesta táctil mejorada. Compatible con consolas Xbox y PC.", 4.9f, 789, R.drawable.xboxcontroller, mockReviewsXbox, "Microsoft (Fabricante)", "Plástico texturizado de alta densidad"),
    Product("CG001", "Computadores Gamers", "PC Gamer ASUS ROG Strix", 1299990, "Un potente equipo diseñado para los gamers más exigentes, equipado con los últimos componentes para ofrecer un rendimiento excepcional en cualquier juego.", 5.0f, 55, R.drawable.pcgamer, mockReviewsPcAsus, "ASUS (Republic of Gamers)", "Metal, vidrio templado, componentes electrónicos"),
    Product("MP001", "Mousepad", "Mousepad Razer Goliathus Extended Chroma", 29990, "Ofrece un área de juego amplia con iluminación RGB personalizable, asegurando una superficie suave y uniforme para el movimiento del mouse.", 4.8f, 312, R.drawable.mousepadrazer, emptyList(), "Razer (Fabricante)", "Tela microtexturizada, base de goma antideslizante"),
    Product("PP001", "Poleras Personalizadas", "Polera Gamer Personalizada 'Level-Up'", 14990, "Una camiseta cómoda y estilizada, con la posibilidad de personalizarla con tu gamer tag o diseño favorito.", 4.5f, 40, R.drawable.polera, emptyList(), "Level-Up Merch (Distribuidor)", "100% Algodón estampado")
)

// -------------------------------------------------------------------------------------
// 4. UTILITARIOS
// -------------------------------------------------------------------------------------

/**
 * Formatea un precio entero a moneda chilena (CLP).
 */
fun formatPrice(price: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    // Elimina decimales y ajusta el símbolo $
    return format.format(price).replace(",00", "").replace("$", "$ ")
}