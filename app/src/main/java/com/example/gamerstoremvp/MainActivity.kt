package com.example.gamerstoremvp

// --- Importaciones Básicas y de Android ---
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.edit
import androidx.core.net.toUri

// --- Importaciones de Compose UI ---
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Importaciones de ViewModel ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// --- Importaciones de Navigation Compose ---
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.gamerstoremvp.core.theme.*

// --- Importaciones de tu Proyecto ---
import com.example.gamerstoremvp.features.auth.AuthScreen
import com.example.gamerstoremvp.features.cart.CartScreen
import com.example.gamerstoremvp.features.cart.CheckoutScreen
import com.example.gamerstoremvp.features.catalog.CatalogScreen
import com.example.gamerstoremvp.features.catalog.ProductDetailScreen
import com.example.gamerstoremvp.features.data.remote.RetrofitClient
import com.example.gamerstoremvp.models.Product
import com.example.gamerstoremvp.features.orders.OrdersScreen
import com.example.gamerstoremvp.features.profile.AboutUsScreen
import com.example.gamerstoremvp.features.profile.ProfileScreen

// --- NUEVA IMPORTACIÓN: MAPA ---
import com.example.gamerstoremvp.features.events.EventsMapScreen

// --- Importaciones de Coroutines ---
import kotlinx.coroutines.launch

// --- Importaciones de GSON ---
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --- Importaciones de tu Proyecto (Tema) ---
import com.example.gamerstoremvp.ui.theme.GamerStoreMVPTheme
import com.example.gamerstoremvp.ui.theme.components.GamerStoreTopBar


// --- ViewModel (ACTUALIZADO con SharedPreferences KTX) ---
class UserViewModel(context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val currentUserKey = "current_user"
    private val allUsersKey = "all_users"
    private val ordersKeyPrefix = "orders_"

    var currentUser: User? by mutableStateOf(loadUser())
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
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun loginUser(user: User) {
        currentUser = user
        try {
            val userJson = gson.toJson(user)
            sharedPreferences.edit { putString(currentUserKey, userJson) }
            loadOrders(user.id)
        } catch (_: Exception) {
        }
    }

    fun logoutUser() {
        currentUser = null
        sharedPreferences.edit { remove(currentUserKey) }
        orders = emptyList()
    }

    private fun loadAllUsers() {
        val usersJson = sharedPreferences.getString(allUsersKey, null)
        val listType = object : TypeToken<List<User>>() {}.type

        val users: List<User>? = try {
            gson.fromJson(usersJson, listType)
        } catch (_: Exception) {
            null
        }

        if (users.isNullOrEmpty()) {
            val defaultUser = User(
                name = "Gamer de Prueba",
                email = "usuario@ejemplo.com",
                password = "123456",
                phone = "+56912345678",
                address = "Av. Siempre Viva 123, Concepción",
                profileImageResId = null,
                levelUpPoints = 5000
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
            sharedPreferences.edit { putString(allUsersKey, usersJson) }
        } catch (_: Exception) {
        }
    }

    fun registerUser(newUser: User, referringUser: User?) {
        var usersToUpdate = this.allUsers

        if (referringUser != null) {
            val updatedReferringUser = referringUser.copy(
                levelUpPoints = referringUser.levelUpPoints + 1000
            )

            usersToUpdate = usersToUpdate.map {
                if (it.id == referringUser.id) updatedReferringUser else it
            }
        }

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
            sharedPreferences.edit { putString("$ordersKeyPrefix$currentUserId", ordersJson) }
            orders = updatedOrders
        } catch (_: Exception) {
        }
    }

    private fun loadOrders(userId: String) {
        val ordersJson = sharedPreferences.getString("$ordersKeyPrefix$userId", null)
        orders = if (ordersJson != null) {
            try {
                val listType = object : TypeToken<List<Order>>() {}.type
                gson.fromJson(ordersJson, listType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}

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

@Composable
fun GamerStoreApp(userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by rememberUpdatedState(userViewModel.currentUser)
    val isAuthenticated = currentUser != null
    val shoppingCart = remember { mutableStateMapOf<Int, Int>() }
    val context = LocalContext.current

    // --- CORRECCIÓN 1: Variable para guardar el total con descuento ---
    var checkoutTotalAmount by remember { mutableIntStateOf(0) }
    // ------------------------------------------------------------------

    val onAddToCart: (Product) -> Unit = { product ->
        shoppingCart[product.id] = (shoppingCart[product.id] ?: 0) + 1
    }
    val onDecreaseQuantity: (Product) -> Unit = { product ->
        val qty = shoppingCart[product.id] ?: 0
        if (qty > 1) shoppingCart[product.id] = qty - 1
        else shoppingCart.remove(product.id)
    }
    val onRemoveFromCart: (Product) -> Unit = { product ->
        shoppingCart.remove(product.id)
    }
    val gson = Gson()

    val onProductClick: (Int) -> Unit = { productId ->
        navController.navigate("${Screen.PRODUCT_DETAIL.name}/$productId")
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

    // --- CORRECCIÓN 2: Modificar la navegación para recibir el total ---
    val onNavigateToCheckout: (Int) -> Unit = { finalTotal ->
        checkoutTotalAmount = finalTotal // Guardamos el precio
        navController.navigate(Screen.CHECKOUT.name)
    }
    // -------------------------------------------------------------------

    val onPaymentSuccess: () -> Unit = {
        currentUser?.let { user ->
            scope.launch {

                val productList = RetrofitClient.instance.getProducts()

                val orderItems = shoppingCart.map { (productId, quantity) ->
                    val product = productList.firstOrNull { it.id == productId }

                    OrderItem(
                        productName = product?.name ?: "Producto desconocido",
                        quantity = quantity,
                        pricePerUnit = product?.price?.toInt() ?: 0
                    )
                }

                val newOrder = Order(
                    items = orderItems,
                    totalAmount = checkoutTotalAmount,
                    userId = user.id
                )

                userViewModel.saveOrder(newOrder)
                shoppingCart.clear()

                navController.navigate(Screen.CATALOG.name) {
                    popUpTo(Screen.CART.name) { inclusive = true }
                    launchSingleTop = true
                }

                Toast.makeText(context, "¡Compra realizada con éxito!", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = ColorPrimaryBackground) {
                Spacer(Modifier.height(12.dp))
                Text("Menú Level-Up", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, color = ColorAccentNeon, fontFamily = Orbitron)
                HorizontalDivider(color= Color.DarkGray)
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, "Productos", tint = ColorTextPrimary) },
                    label = { Text("Productos", color = ColorTextPrimary) },
                    selected = currentRoute == Screen.CATALOG.name,
                    onClick = { scope.launch { drawerState.close() }; navController.navigate(Screen.CATALOG.name) { launchSingleTop = true } },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                // --- Opción Mapa de Eventos ---
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Map, "Mapa de Eventos", tint = ColorTextPrimary) },
                    label = { Text("Mapa de Eventos", color = ColorTextPrimary) },
                    selected = currentRoute == Screen.EVENTS_MAP.name,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.EVENTS_MAP.name) {
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                // -------------------------------

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
                            val intent = Intent(Intent.ACTION_VIEW, "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}".toUri())
                            context.startActivity(intent)
                        } catch (_: Exception) { Toast.makeText(context, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show() }
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
                                IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = ColorTextPrimary) }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, "Abrir Menú", tint = ColorTextPrimary) }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (isAuthenticated && currentRoute != Screen.AUTH.name && currentRoute != Screen.PRODUCT_DETAIL.name && currentRoute != Screen.CHECKOUT.name) {
                    BottomNavigationBar(navController = navController, currentRoute = currentRoute)
                }
            },
            containerColor = ColorPrimaryBackground
        ) { innerPadding ->
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
                composable(Screen.CATALOG.name) {
                    CatalogScreen(
                        onProductClick = onProductClick,
                        onAddToCart = { productId ->
                            shoppingCart[productId] = (shoppingCart[productId] ?: 0) + 1
                        }
                    )
                }
                composable(Screen.CART.name) {
                    val userEmail = userViewModel.currentUser?.email ?: ""
                    CartScreen(
                        cart = shoppingCart.toMap(),
                        onRemoveFromCart = { productId -> shoppingCart.remove(productId) },
                        onIncreaseQuantity = { productId ->
                            shoppingCart[productId] = (shoppingCart[productId] ?: 0) + 1
                        },
                        onDecreaseQuantity = { productId ->
                            val qty = shoppingCart[productId] ?: 0
                            if (qty > 1) shoppingCart[productId] = qty - 1
                            else shoppingCart.remove(productId)
                        },
                        onProductClick = { productId ->
                            navController.navigate("${Screen.PRODUCT_DETAIL.name}/$productId")
                        },
                        onNavigateToCheckout = onNavigateToCheckout,
                        userEmail = userEmail
                    )
                }
                composable("${Screen.PRODUCT_DETAIL.name}/{productId}") { backStackEntry ->

                    val productId = backStackEntry.arguments
                        ?.getString("productId")
                        ?.toIntOrNull()

                    if (productId != null) {

                        // ✅ CLAVE PARA QUE NO SE REUTILICE EL VIEWMODEL
                        key(productId) {
                            ProductDetailScreen(
                                productCode = productId.toString(),
                                onAddToCart = { product -> onAddToCart(product) },
                                onDecreaseQuantity = { product -> onDecreaseQuantity(product) },
                                cart = shoppingCart.toMap()
                            )
                        }

                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Screen.PROFILE.name) {
                    currentUser?.let { user ->
                        ProfileScreen(user = user, onLogout = onLogout, onUserUpdate = onUserUpdate)
                    } ?: run { LaunchedEffect(Unit) { navController.navigate(Screen.AUTH.name) { popUpTo(0)} } }
                }

                // --- CORRECCIÓN 5: Pasamos el total guardado al Checkout ---
                composable(Screen.CHECKOUT.name) {
                    CheckoutScreen(totalAmount = checkoutTotalAmount, onPaymentSuccess = onPaymentSuccess)
                }
                // ------------------------------------------------------------

                composable(Screen.ORDERS.name) {
                    OrdersScreen(orders = userViewModel.orders)
                }
                composable(Screen.ABOUT_US.name) {
                    AboutUsScreen()
                }

                composable(Screen.EVENTS_MAP.name) {
                    EventsMapScreen()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.DarkGray.copy(alpha=0.9f)) {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, "Productos", tint = if (currentRoute == Screen.CATALOG.name) ColorAccentNeon else ColorTextSecondary) },
            label = { Text("Productos", color = if (currentRoute == Screen.CATALOG.name) ColorAccentNeon else ColorTextSecondary, fontSize = 10.sp) },
            selected = currentRoute == Screen.CATALOG.name,
            onClick = {
                navController.navigate(Screen.CATALOG.name) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = ColorAccentNeon.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = {
                val icon = Icons.Filled.AccountCircle
                val color = if (currentRoute == Screen.PROFILE.name) ColorAccentNeon else ColorTextSecondary
                Icon(icon, "Perfil", tint = color)
            },
            label = {
                val text = "Perfil"
                val color = if (currentRoute == Screen.PROFILE.name) ColorAccentNeon else ColorTextSecondary
                Text(text, color = color, fontSize = 10.sp)
            },
            selected = currentRoute == Screen.PROFILE.name,
            onClick = {
                navController.navigate(Screen.PROFILE.name) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = ColorAccentNeon.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Pedidos", tint = if (currentRoute == Screen.ORDERS.name) ColorAccentNeon else ColorTextSecondary) },
            label = { Text("Pedidos", color = if (currentRoute == Screen.ORDERS.name) ColorAccentNeon else ColorTextSecondary, fontSize = 10.sp) },
            selected = currentRoute == Screen.ORDERS.name,
            onClick = {
                navController.navigate(Screen.ORDERS.name) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = ColorAccentNeon.copy(alpha = 0.1f))
        )
    }
}
