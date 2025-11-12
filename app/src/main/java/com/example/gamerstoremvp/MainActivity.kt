package com.example.gamerstoremvp

// --- Importaciones Básicas y de Android ---
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.edit // <-- ¡IMPORTANTE PARA SharedPreferences KTX!

// --- Importaciones de Compose UI ---
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // <-- ¡¡IMPORTACIÓN AÑADIDA!! (Arregla error 'sp')

// --- Importaciones de ViewModel ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// --- Importaciones de Navigation Compose ---
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

// --- Importaciones de Coroutines ---
import kotlinx.coroutines.launch

// --- Importaciones de GSON ---
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --- Importaciones de tu Proyecto ---
import com.example.gamerstoremvp.ui.theme.GamerStoreMVPTheme
import com.example.gamerstoremvp.User
import com.example.gamerstoremvp.Product
import com.example.gamerstoremvp.Screen
import com.example.gamerstoremvp.Order
import com.example.gamerstoremvp.OrderItem
import com.example.gamerstoremvp.mockProducts
import com.example.gamerstoremvp.AuthScreen
import com.example.gamerstoremvp.CatalogScreen
import com.example.gamerstoremvp.CartScreen
import com.example.gamerstoremvp.ProductDetailScreen
import com.example.gamerstoremvp.ProfileScreen
import com.example.gamerstoremvp.CheckoutScreen
import com.example.gamerstoremvp.OrdersScreen
import com.example.gamerstoremvp.AboutUsScreen
import com.example.gamerstoremvp.GamerStoreTopBar
import com.example.gamerstoremvp.ColorPrimaryBackground
import com.example.gamerstoremvp.ColorAccentNeon
import com.example.gamerstoremvp.ColorTextPrimary
import com.example.gamerstoremvp.ColorTextSecondary
import com.example.gamerstoremvp.ColorAccentBlue
import com.example.gamerstoremvp.Orbitron
import com.example.gamerstoremvp.Roboto


// --- ViewModel (ACTUALIZADO con SharedPreferences KTX) ---
class UserViewModel(context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val currentUserKey = "current_user"
    private val allUsersKey = "all_users"
    private val ordersKeyPrefix = "orders_"

    var currentUser by mutableStateOf<User?>(loadUser())
        private set

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    var allUsers by mutableStateOf<List<User>>(emptyList())
        private set

    init {
        currentUser?.let { loadOrders(it.id) }
        loadAllUsers()
    }

    private fun loadUser(): User? {
        val userJson = sharedPreferences.getString(currentUserKey, null)
        return if (userJson != null) {
            try { gson.fromJson(userJson, User::class.java) } catch (e: Exception) { println("Error loading user: ${e.message}"); null }
        } else { null }
    }

    fun loginUser(user: User) {
        currentUser = user
        try {
            val userJson = gson.toJson(user)
            // Forma KTX (corregido)
            sharedPreferences.edit { putString(currentUserKey, userJson) }
            loadOrders(user.id)
        } catch (e: Exception) { println("Error saving user: ${e.message}") }
    }

    fun logoutUser() {
        currentUser = null
        // Forma KTX (corregido)
        sharedPreferences.edit { remove(currentUserKey) }
        orders = emptyList()
    }

    private fun loadAllUsers() {
        val usersJson = sharedPreferences.getString(allUsersKey, null)
        val listType = object : TypeToken<List<User>>() {}.type

        val users: List<User>? = try { gson.fromJson(usersJson, listType) } catch (e: Exception) { null }

        if (users.isNullOrEmpty()) {
            val defaultUser = User(
                name = "Gamer de Prueba",
                email = "usuario@ejemplo.com",
                password = "123456",
                phone = "+56912345678",
                address = "Av. Siempre Viva 123, Concepción",
                profileImageResId = null,
                levelUpPoints = 5000 // Le daremos puntos de bienvenida al de prueba
            )
            allUsers = listOf(defaultUser)
            saveAllUsers()
        } else {
            allUsers = users
        }
    }

    private fun saveAllUsers() {
        try {
            val usersJson = gson.toJson(allUsers)
            // Forma KTX (corregido)
            sharedPreferences.edit { putString(allUsersKey, usersJson) }
        } catch (e: Exception) { println("Error saving all users: ${e.message}") }
    }

    // --- ¡¡FUNCIÓN ACTUALIZADA!! ---
    // Ahora acepta al usuario que refirió para darle puntos
    fun registerUser(newUser: User, referringUser: User?) {
        var usersToUpdate = this.allUsers

        if (referringUser != null) {
            // Recompensa para el usuario que refirió
            // (1000 puntos es un premio lógico)
            val updatedReferringUser = referringUser.copy(
                levelUpPoints = referringUser.levelUpPoints + 1000
            )

            // Reemplaza al usuario antiguo por el actualizado en la lista
            usersToUpdate = usersToUpdate.map {
                if (it.id == referringUser.id) updatedReferringUser else it
            }
        }

        // Añade el nuevo usuario (que ya tiene sus puntos base + bono)
        this.allUsers = usersToUpdate + newUser
        saveAllUsers()
    }

    fun updateUser(updatedUser: User) {
        if (currentUser?.id == updatedUser.id) {
            loginUser(updatedUser)
            allUsers = allUsers.map {
                if (it.id == updatedUser.id) updatedUser else it
            }
            saveAllUsers()
        }
    }

    fun saveOrder(order: Order) {
        val currentUserId = currentUser?.id ?: return
        val updatedOrders = orders + order
        try {
            val ordersJson = gson.toJson(updatedOrders)
            // Forma KTX (corregido)
            sharedPreferences.edit { putString("$ordersKeyPrefix$currentUserId", ordersJson) }
            orders = updatedOrders
        } catch (e: Exception) { println("Error saving orders: ${e.message}") }
    }

    private fun loadOrders(userId: String) {
        val ordersJson = sharedPreferences.getString("$ordersKeyPrefix$userId", null)
        orders = if (ordersJson != null) {
            try {
                val listType = object : TypeToken<List<Order>>() {}.type
                gson.fromJson(ordersJson, listType) ?: emptyList()
            } catch (e: Exception) { println("Error loading orders: ${e.message}"); emptyList() }
        } else { emptyList() }
    }
}
// --- Fin del ViewModel ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamerStoreMVPTheme {
                val context = LocalContext.current
                val userViewModel: UserViewModel = viewModel { UserViewModel(context.applicationContext) }
                GamerStoreApp(userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamerStoreApp(userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by rememberUpdatedState(userViewModel.currentUser)
    val isAuthenticated = currentUser != null
    val shoppingCart = remember { mutableStateMapOf<Product, Int>() }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val context = LocalContext.current

    // --- Lógica de Negocio (sin cambios) ---
    val onAddToCart: (Product) -> Unit = { shoppingCart[it] = (shoppingCart[it] ?: 0) + 1 }
    val onDecreaseQuantity: (Product) -> Unit = {
        val qty = shoppingCart[it] ?: 0
        if (qty > 1) shoppingCart[it] = qty - 1 else shoppingCart.remove(it)
    }
    val onRemoveFromCart: (Product) -> Unit = { shoppingCart.remove(it) }
    val onProductClick: (Product) -> Unit = { product ->
        selectedProduct = product
        navController.navigate(Screen.PRODUCT_DETAIL.name)
    }
    val onAuthSuccess: (User) -> Unit = { user ->
        userViewModel.loginUser(user)
        navController.navigate(Screen.CATALOG.name) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }
    val onLogout: () -> Unit = {
        userViewModel.logoutUser()
        shoppingCart.clear()
        navController.navigate(Screen.AUTH.name) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }
    val onUserUpdate: (User) -> Unit = { updatedUser ->
        userViewModel.updateUser(updatedUser)
        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
    }
    val onNavigateToCheckout: () -> Unit = {
        navController.navigate(Screen.CHECKOUT.name)
    }
    val onPaymentSuccess: () -> Unit = {
        currentUser?.let { user ->
            val orderItems = shoppingCart.map { (product, quantity) ->
                OrderItem(productName = product.name, quantity = quantity, pricePerUnit = product.price)
            }
            val totalAmount = orderItems.sumOf { it.pricePerUnit * it.quantity }
            val newOrder = Order(items = orderItems, totalAmount = totalAmount, userId = user.id)
            userViewModel.saveOrder(newOrder)
            shoppingCart.clear()
            navController.navigate(Screen.CATALOG.name) {
                popUpTo(Screen.CART.name) { inclusive = true }
                launchSingleTop = true
            }
            Toast.makeText(context, "¡Compra realizada con éxito!", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(context, "Error: No se pudo registrar el pedido.", Toast.LENGTH_SHORT).show()
            shoppingCart.clear()
            navController.navigate(Screen.CATALOG.name) { popUpTo(Screen.CART.name) { inclusive = true }; launchSingleTop = true }
        }
    }
    // --- Fin Lógica ---

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = ColorPrimaryBackground) {
                Spacer(Modifier.height(12.dp))
                Text("Menú Level-Up", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, color = ColorAccentNeon, fontFamily = Orbitron)
                // --- ¡CORREGIDO! Divider obsoleto cambiado a HorizontalDivider ---
                HorizontalDivider(color= Color.DarkGray)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, "Productos", tint = ColorTextPrimary) },
                    label = { Text("Productos", color = ColorTextPrimary) },
                    selected = currentRoute == Screen.CATALOG.name,
                    onClick = { scope.launch { drawerState.close() }; navController.navigate(Screen.CATALOG.name) { launchSingleTop = true } },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, "Sobre Nosotros", tint = ColorTextPrimary) },
                    label = { Text("Sobre Nosotros", color = ColorTextPrimary) },
                    selected = currentRoute == Screen.ABOUT_US.name,
                    onClick = { scope.launch { drawerState.close() }; navController.navigate(Screen.ABOUT_US.name) { launchSingleTop = true } },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.SupportAgent, "Soporte", tint = ColorTextPrimary) },
                    label = { Text("Soporte", color = ColorTextPrimary) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val phoneNumber = "+56941454288"
                        val message = "¡Hola! Necesito soporte técnico de Level-Up Gamer."
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"))
                            context.startActivity(intent)
                        } catch (e: Exception) { Toast.makeText(context, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isAuthenticated && currentRoute != Screen.AUTH.name) {
                    val canGoBack = navController.previousBackStackEntry != null && currentRoute != Screen.CATALOG.name
                    GamerStoreTopBar(
                        onNavigateToCart = { navController.navigate(Screen.CART.name) },
                        onNavigateToCatalog = {
                            navController.navigate(Screen.CATALOG.name) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        cartItemCount = shoppingCart.values.sum(),
                        navigationIcon = {
                            if (canGoBack) {
                                IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Filled.ArrowBack, "Volver", tint = ColorTextPrimary) }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, "Abrir Menú", tint = ColorTextPrimary) }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (isAuthenticated && currentRoute != Screen.AUTH.name && currentRoute != Screen.PRODUCT_DETAIL.name && currentRoute != Screen.CHECKOUT.name) {
                    BottomNavigationBar(navController = navController, currentRoute = currentRoute, isAuthenticated = isAuthenticated)
                }
            },
            containerColor = ColorPrimaryBackground
        ) { innerPadding ->
            // --- NavHost ---
            NavHost(
                navController = navController,
                startDestination = if (userViewModel.currentUser == null) Screen.AUTH.name else Screen.CATALOG.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.AUTH.name) {
                    AuthScreen(
                        userViewModel = userViewModel,
                        onAuthSuccess = onAuthSuccess
                    )
                }
                composable(Screen.CATALOG.name) { CatalogScreen(products = mockProducts, onAddToCart = onAddToCart, onProductClick = onProductClick) }

                composable(Screen.CART.name) {
                    val userEmail = userViewModel.currentUser?.email ?: ""
                    CartScreen(
                        cart = shoppingCart,
                        onRemoveFromCart = onRemoveFromCart,
                        onIncreaseQuantity = onAddToCart,
                        onDecreaseQuantity = onDecreaseQuantity,
                        onProductClick = onProductClick,
                        onNavigateToCheckout = onNavigateToCheckout,
                        userEmail = userEmail
                    )
                }
                composable(Screen.PRODUCT_DETAIL.name) {
                    selectedProduct?.let { product ->
                        ProductDetailScreen(product = product, onAddToCart = onAddToCart, onDecreaseQuantity = onDecreaseQuantity, cart = shoppingCart)
                    } ?: run { LaunchedEffect(Unit) { navController.popBackStack() } }
                }
                composable(Screen.PROFILE.name) {
                    currentUser?.let { user ->
                        ProfileScreen(user = user, onLogout = onLogout, onUserUpdate = onUserUpdate)
                    } ?: run { LaunchedEffect(Unit) { navController.navigate(Screen.AUTH.name) { popUpTo(0)} } }
                }
                composable(Screen.CHECKOUT.name) {
                    val total = shoppingCart.entries.sumOf { it.key.price * it.value }
                    CheckoutScreen(totalAmount = total, onPaymentSuccess = onPaymentSuccess)
                }
                composable(Screen.ORDERS.name) {
                    OrdersScreen(orders = userViewModel.orders)
                }
                composable(Screen.ABOUT_US.name) {
                    AboutUsScreen()
                }
            }
            // --- Fin NavHost ---
        }
    }
}

// --- Composable Barra Inferior (CORREGIDO) ---
@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?, isAuthenticated: Boolean) {
    NavigationBar(containerColor = Color.DarkGray.copy(alpha=0.9f)) {
        // --- Item Productos ---
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, "Productos", tint = if (currentRoute == Screen.CATALOG.name) ColorAccentNeon else ColorTextSecondary) },
            label = { Text("Productos", color = if (currentRoute == Screen.CATALOG.name) ColorAccentNeon else ColorTextSecondary, fontSize = 10.sp) },
            selected = currentRoute == Screen.CATALOG.name,
            onClick = {
                navController.navigate(Screen.CATALOG.name) {
                    // ESTA ES LA LÓGICA CORRECTA
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = ColorAccentNeon.copy(alpha = 0.1f))
        )
        // --- Item Perfil/Login ---
        NavigationBarItem(
            icon = {
                val icon = if (isAuthenticated) Icons.Filled.AccountCircle else Icons.Filled.Login
                val color = if (currentRoute == Screen.PROFILE.name || currentRoute == Screen.AUTH.name) ColorAccentNeon else ColorTextSecondary
                Icon(icon, "Perfil/Login", tint = color)
            },
            label = {
                val text = if (isAuthenticated) "Perfil" else "Login"
                val color = if (currentRoute == Screen.PROFILE.name || currentRoute == Screen.AUTH.name) ColorAccentNeon else ColorTextSecondary
                Text(text, color = color, fontSize = 10.sp)
            },
            selected = currentRoute == Screen.PROFILE.name || currentRoute == Screen.AUTH.name,
            onClick = {
                val destination = if (isAuthenticated) Screen.PROFILE.name else Screen.AUTH.name
                navController.navigate(destination) {
                    // ESTA ES LA LÓGICA CORRECTA
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = ColorAccentNeon.copy(alpha = 0.1f))
        )
        // --- Item Pedidos ---
        NavigationBarItem(
            icon = { Icon(Icons.Filled.ReceiptLong, "Pedidos", tint = if (currentRoute == Screen.ORDERS.name) ColorAccentNeon else ColorTextSecondary) },
            label = { Text("Pedidos", color = if (currentRoute == Screen.ORDERS.name) ColorAccentNeon else ColorTextSecondary, fontSize = 10.sp) },
            selected = currentRoute == Screen.ORDERS.name,
            onClick = {
                navController.navigate(Screen.ORDERS.name) {
                    // ESTA ES LA LÓGICA CORRECTA
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = ColorAccentNeon.copy(alpha = 0.1f))
        )
    }
}