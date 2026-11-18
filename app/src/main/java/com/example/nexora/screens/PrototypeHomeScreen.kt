package com.example.nexora.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

data class PrototypeListing(
    val title: String,
    val priceLabel: String,
    val category: String,
    val sellerLabel: String,
    val imageUrl: String
)

@Composable
fun PrototypeHomeScreen(
    onListingClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyProductsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val listings = listOf(
        PrototypeListing(
            title = "Tutorías de Matemática",
            priceLabel = "Q75 / hora",
            category = "Tutorías",
            sellerLabel = "Vendedor verificado",
            imageUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=600&q=80"
        ),
        PrototypeListing(
            title = "Brownies caseros",
            priceLabel = "Q25",
            category = "Comida",
            sellerLabel = "Vendedor verificado",
            imageUrl = "https://images.unsplash.com/photo-1607920591413-4ec007e70023?auto=format&fit=crop&w=600&q=80"
        ),
        PrototypeListing(
            title = "Soporte técnico para computadoras",
            priceLabel = "Q100",
            category = "Tecnología",
            sellerLabel = "Vendedor verificado",
            imageUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=600&q=80"
        ),
        PrototypeListing(
            title = "Diseño gráfico para redes sociales",
            priceLabel = "Q150",
            category = "Diseño",
            sellerLabel = "Vendedor verificado",
            imageUrl = "https://images.unsplash.com/photo-1561070791-2526d30994b5?auto=format&fit=crop&w=600&q=80"
        )
    )

    val filteredListings = listings.filter { listing ->
        val matchesSearch = listing.title.contains(
            searchQuery,
            ignoreCase = true
        )

        val matchesCategory = selectedCategory == "Todos" ||
                listing.category == selectedCategory

        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPublishClick,
                containerColor = Color(0xFF17345F),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "☷",
                    fontSize = 24.sp
                )
            }
        },
        bottomBar = {
            PrototypeBottomBar(
                onMyProductsClick = onMyProductsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(contentPadding)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inicio",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDE7F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("¿Qué estás buscando?")
                },
                leadingIcon = {
                    Text(
                        text = "⌕",
                        fontSize = 22.sp
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Categorías",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrototypeCategoryItem(
                    name = "Todos",
                    symbol = "★",
                    isSelected = selectedCategory == "Todos",
                    onClick = {
                        selectedCategory = "Todos"
                    }
                )

                PrototypeCategoryItem(
                    name = "Tutorías",
                    symbol = "📚",
                    isSelected = selectedCategory == "Tutorías",
                    onClick = {
                        selectedCategory = "Tutorías"
                    }
                )

                PrototypeCategoryItem(
                    name = "Comida",
                    symbol = "🍰",
                    isSelected = selectedCategory == "Comida",
                    onClick = {
                        selectedCategory = "Comida"
                    }
                )

                PrototypeCategoryItem(
                    name = "Tecnología",
                    symbol = "💻",
                    isSelected = selectedCategory == "Tecnología",
                    onClick = {
                        selectedCategory = "Tecnología"
                    }
                )

                PrototypeCategoryItem(
                    name = "Diseño",
                    symbol = "🎨",
                    isSelected = selectedCategory == "Diseño",
                    onClick = {
                        selectedCategory = "Diseño"
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recomendados para ti",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ver todos",
                    fontSize = 13.sp,
                    color = Color(0xFF17345F)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 75.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredListings) { listing ->
                    PrototypeListingCard(
                        listing = listing,
                        onClick = onListingClick
                    )
                }
            }
        }
    }
}

@Composable
fun PrototypeCategoryItem(
    name: String,
    symbol: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) {
                    Color(0xFF17345F)
                } else {
                    Color(0xFFE4EBF3)
                },
                contentColor = if (isSelected) {
                    Color.White
                } else {
                    Color(0xFF25364A)
                }
            )
        ) {
            Text(
                text = symbol,
                fontSize = 19.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 10.sp
        )
    }
}

@Composable
fun PrototypeListingCard(
    listing: PrototypeListing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.92f)
                        )
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♡",
                        fontSize = 21.sp
                    )
                }
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = listing.title,
                    fontSize = 13.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = listing.priceLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "✓ ${listing.sellerLabel}",
                    fontSize = 10.sp,
                    color = Color(0xFF238B57)
                )
            }
        }
    }
}

@Composable
fun PrototypeBottomBar(
    onMyProductsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PrototypeBottomBarItem(
            symbol = "⌂",
            label = "Inicio"
        )

        PrototypeBottomBarItem(
            symbol = "⌕",
            label = "Búsqueda"
        )

        PrototypeBottomBarItem(
            symbol = "▣",
            label = "Mis Productos",
            onClick = onMyProductsClick
        )

        PrototypeBottomBarItem(
            symbol = "●",
            label = "Perfil",
            onClick = onProfileClick
        )
    }
}

@Composable
fun PrototypeBottomBarItem(
    symbol: String,
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val itemModifier = if (onClick != null) {
        modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    } else {
        modifier
    }

    Column(
        modifier = itemModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = symbol,
            fontSize = 18.sp,
            color = Color(0xFF17345F)
        )

        Text(
            text = label,
            fontSize = 9.sp
        )
    }
}