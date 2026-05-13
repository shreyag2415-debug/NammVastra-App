package com.nammavastra

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import coil.compose.AsyncImage
import com.nammavastra.FirebaseHelper
import com.nammavastra.HistoryData
import com.nammavastra.SareeData
import com.nammavastra.TrendData
import kotlinx.coroutines.launch

@Composable
fun TrendScreen() {
    val trends = remember { mutableStateListOf<TrendData>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val fetchedTrends = FirebaseHelper.fetchTrends()
            trends.clear()
            trends.addAll(fetchedTrends)
        }
    }

    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {

        Text("Fashion Insights", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (trends.isEmpty()) {
            // Default Enhanced trends
            TrendCard("The Pastel Era", "City boutiques want lavender and sage green.", Color(0xFFE6E6FA))
            Spacer(modifier = Modifier.height(16.dp))
            TrendCard("Sunset Ochre", "Golden hour shades are the top choice for summer weddings.", Color(0xFFCC7722))
            Spacer(modifier = Modifier.height(16.dp))
            TrendCard("Royal Indigo", "Authentic vegetable dyes are back in high demand.", Color(0xFF00416A))
            Spacer(modifier = Modifier.height(16.dp))
            TrendCard("Rose Quartz", "A soft, romantic pink trending for contemporary drapes.", Color(0xFFF7CAC9))
        } else {
            trends.forEach { trend ->
                TrendCard(trend.title, trend.desc, Color(android.graphics.Color.parseColor(trend.colorHex)))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TrendCard(title: String, desc: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(color))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun GalleryScreen(wishlist: List<SareeData>, onToggleWishlist: (SareeData) -> Unit) {
    val sarees = remember { mutableStateListOf<SareeData>() }
    val scope = rememberCoroutineScope()
    var filterBy by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        scope.launch {
            val fetchedSarees = FirebaseHelper.fetchSarees()
            sarees.clear()
            sarees.addAll(fetchedSarees)
        }
    }

    val filteredSarees = if (filterBy == "All") sarees else sarees.filter { it.material.contains(filterBy, ignoreCase = true) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            listOf("All", "Silk", "Cotton", "Khadi").forEach { material ->
                FilterChip(
                    selected = filterBy == material,
                    onClick = { filterBy = material },
                    label = { Text(material) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredSarees.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF5A5A40))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredSarees) { saree ->
                    SareeCard(
                        saree = saree,
                        isWishlisted = wishlist.any { it.name == saree.name },
                        onToggleWishlist = { onToggleWishlist(saree) }
                    )
                }
            }
        }
    }
}

@Composable
fun SareeCard(saree: SareeData, isWishlisted: Boolean, onToggleWishlist: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Box {
            AsyncImage(
                model = saree.imageUrl,
                contentDescription = saree.name,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onToggleWishlist,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(
                    if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isWishlisted) Color.Red else Color.White
                )
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(saree.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("₹${saree.price}", color = Color(0xFF5A5A40))
        }
    }
}

@Composable
fun WishlistScreen(wishlist: List<SareeData>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Wishlist", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (wishlist.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, Modifier.size(100.dp), tint = Color.LightGray)
                Text("Your Wishlist is Empty", style = MaterialTheme.typography.titleLarge)
                Text("Heart your favorite sarees in the Loom to see them here.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(wishlist) { saree ->
                    SareeCard(
                        saree = saree,
                        isWishlisted = true,
                        onToggleWishlist = { /* Wishlist screen can toggle too if passed, but keeping it simple */ }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var length by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Price Estimator", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text("Length (in meters)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { result = (length.toIntOrNull() ?: 0) * 450 },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A5A40)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Calculate Estimate", fontWeight = FontWeight.Bold)
        }

        if (result > 0) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Estimated Price:", style = MaterialTheme.typography.bodyMedium)
                    Text("₹$result", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/919876543210?text=I am looking for a saree of $length meters. Estimated price: ₹$result")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("Share Quote on WhatsApp")
                    }
                }
            }
        }
    }
}

@Composable
fun UploadScreen() {
    var name by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Upload New Saree", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Add a masterpiece to your digital loom gallery.", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Saree Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (Rupees)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("Weaver WhatsApp") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image Link (URL)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && price.isNotEmpty()) {
                    isUploading = true
                    scope.launch {
                        val success = FirebaseHelper.addSaree(SareeData(name, material, price.toIntOrNull() ?: 0, whatsapp, imageUrl))
                        isUploading = false
                        if (success) {
                            android.widget.Toast.makeText(context, "Uploaded!", android.widget.Toast.LENGTH_SHORT).show()
                            name = ""; material = ""; price = ""; imageUrl = ""; whatsapp = ""
                        }
                    }
                }
            },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A5A40)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Upload Saree")
        }
    }
}

@Composable
fun StoryScreen() {
    val stories = remember { mutableStateListOf<HistoryData>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val fetchedHistory = FirebaseHelper.fetchHistory()
            stories.clear()
            stories.addAll(fetchedHistory)
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("Heritage Stories", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (stories.isEmpty()) {
            // Default Heritage Stories & User requested specific ones
            StoryItem(
                "Emerald Gombe Silk",
                "Inspired by the traditional wooden dolls (Gombes) of Channapatna. This silk features vibrant forest greens and playful border patterns that mimic the lacquerware toys.",
                "https://images.unsplash.com/photo-1610398000003-0d19ed2c76bc?q=80&w=800"
            )
            StoryItem(
                "Ivory Summer Cotton",
                "A tribute to the scorching summers of the Deccan plateau. The pure ivory cotton is breathable and features a simple gold zari, representing grace and cooling comfort.",
                "https://images.unsplash.com/photo-1590736934524-1925b4131599?q=80&w=800"
            )
            StoryItem(
                "Midnight Khadi Plain",
                "Hand-spun and hand-woven, this charcoal black Khadi represents the resilience of the rural weaver. It is a statement of raw, unpolished elegance for the modern woman.",
                "https://images.unsplash.com/photo-1528459801416-a7e99b084945?q=80&w=800"
            )
            StoryItem(
                "The Legend of Ilkal",
                "Tracing back to the 8th century, the Ilkal saree is known for its 'Topi Teni' technique, where the body and pallu are joined with a special series of loops.",
                "https://images.unsplash.com/photo-1605342432924-f7b539d916ae?q=80&w=800"
            )
        } else {
            stories.forEach { story ->
                StoryItem(story.title, story.content, story.imageUrl)
            }
        }
    }
}

@Composable
fun StoryItem(title: String, content: String, imageUrl: String) {
    Column {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (UserRole) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFDFCF8)) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Namma Vastra App 🙏", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("The Weaver's Digital Catalog", color = Color.Gray)

            Spacer(modifier = Modifier.height(64.dp))

            Text("Continue as:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onLoginSuccess(UserRole.WEAVER) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A5A40))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("I am a Weaver")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onLoginSuccess(UserRole.BUYER) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF5A5A40))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF5A5A40))
                    Spacer(Modifier.width(8.dp))
                    Text("I am a Buyer", color = Color(0xFF5A5A40))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Weavers get access to upload their products.\nBuyers can view and wishlist items.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
