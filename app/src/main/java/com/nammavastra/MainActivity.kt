package com.nammavastra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.navigation.compose.*
import androidx.compose.ui.unit.dp
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Trends : Screen("trends", "Trends", Icons.Default.TrendingUp)
    object Gallery : Screen("gallery", "Loom", Icons.Default.ShoppingBag)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Default.Favorite)
    object Upload : Screen("upload", "Upload", Icons.Default.Add)
    object Calculator : Screen("calculator", "Price", Icons.Default.Calculate)
    object Story : Screen("story", "History", Icons.Default.AutoStories)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NammaVastraTheme {
                MainContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf(UserRole.GUEST) }
    var isOnline by remember { mutableStateOf(true) }
    val wishlist = remember { mutableStateListOf<SareeData>() }

    val items = remember(userRole) {
        if (userRole == UserRole.WEAVER) {
            listOf(Screen.Trends, Screen.Gallery, Screen.Wishlist, Screen.Upload, Screen.Calculator, Screen.Story)
        } else {
            listOf(Screen.Trends, Screen.Gallery, Screen.Calculator, Screen.Story, Screen.Wishlist)
        }
    }

    // Monitor connectivity
    LaunchedEffect(Unit) {
        while(true) {
            isOnline = withContext(Dispatchers.IO) {
                try {
                    val address = InetAddress.getByName("google.com")
                    !address.equals("")
                } catch (e: Exception) {
                    false
                }
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { role ->
            userRole = role
            isLoggedIn = true
        })
    } else {
        Scaffold(
            topBar = {
                Column {
                    if (!isOnline) {
                        Surface(color = Color.Red, modifier = Modifier.fillMaxWidth()) {
                            Text("Offline Mode - Showing Cached Data", color = Color.White, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    CenterAlignedTopAppBar(
                        title = { Text("Namma Vastra", style = MaterialTheme.typography.headlineMedium) },
                        actions = {
                            if (userRole == UserRole.WEAVER) {
                                IconButton(onClick = { navController.navigate(Screen.Upload.route) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Upload")
                                }
                            }
                            IconButton(onClick = {
                                isLoggedIn = false
                                userRole = UserRole.GUEST
                            }) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (userRole == UserRole.WEAVER) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.Upload.route) },
                        containerColor = Color(0xFF5A5A40),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Upload")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = Screen.Trends.route, Modifier.padding(innerPadding)) {
                composable(Screen.Trends.route) { TrendScreen() }
                composable(Screen.Gallery.route) {
                    GalleryScreen(
                        wishlist = wishlist,
                        onToggleWishlist = { saree ->
                            if (wishlist.any { it.name == saree.name }) {
                                wishlist.removeAll { it.name == saree.name }
                            } else {
                                wishlist.add(saree)
                            }
                        }
                    )
                }
                composable(Screen.Wishlist.route) { WishlistScreen(wishlist) }
                composable(Screen.Upload.route) { UploadScreen() }
                composable(Screen.Calculator.route) { CalculatorScreen() }
                composable(Screen.Story.route) { StoryScreen() }
            }
        }
    }
}
