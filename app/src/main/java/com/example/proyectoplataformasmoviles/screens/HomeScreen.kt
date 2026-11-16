package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.proyectoplataformasmoviles.R
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.presentation.home.HomeUiState
import com.example.proyectoplataformasmoviles.ui.theme.Espaciado
import com.example.proyectoplataformasmoviles.domain.model.formatearImporte as formatPrice

@Composable
fun HomeScreen(
    state: HomeUiState,
    onListingClick: (String) -> Unit,
    onPublishClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "Visitante",
    userRole: String = "Estudiante",
    onProfileClick: () -> Unit = {},
    necesitaAcceso: Boolean = false,
    listingImageContent: @Composable (Listing, Modifier) -> Unit = { listing, imageModifier ->
        DefaultListingImage(
            listing = listing,
            modifier = imageModifier
        )
    }
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var selectedCategory by rememberSaveable {
        mutableStateOf("Todos")
    }

    val categories = listOf(
        "Todos",
        "Tutorías",
        "Tecnología",
        "Diseño",
        "Comida",
        "Consultoría"
    )

    val filteredListings = state.listings.filter { listing ->
        val matchesCategory = selectedCategory == "Todos" ||
                listing.category.equals(
                    selectedCategory,
                    ignoreCase = true
                )

        val matchesSearch = searchQuery.isBlank() ||
                "${listing.title} ${listing.description} ${listing.sellerName}"
                    .contains(
                        searchQuery.trim(),
                        ignoreCase = true
                    )

        matchesCategory && matchesSearch
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_inicio),
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Inicio")
                    }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onPublishClick,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_publicar),
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Publicar")
                    }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_perfil),
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Perfil")
                    }
                )
            }
        }
    ) { contentPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(
                Espaciado.mediano
            ),
            verticalArrangement = Arrangement.spacedBy(
                Espaciado.normal
            )
        ) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MarcaNexora()

                    FilledTonalIconButton(
                        onClick = onProfileClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_perfil),
                            contentDescription = "Abrir mi perfil"
                        )
                    }
                }
            }

            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        Espaciado.pequeno
                    )
                ) {
                    Text(
                        text = "Hola, $userName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Descubrí el talento\nde tu comunidad.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Estudiantes de todas las universidades de Guatemala.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    singleLine = true,
                    placeholder = {
                        Text("¿Qué estás buscando?")
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_buscar),
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        Espaciado.pequeno
                    )
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                            },
                            label = {
                                Text(category)
                            }
                        )
                    }
                }
            }

            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            state.isLoading ->
                                "Cargando ofertas"

                            state.errorMessage != null ->
                                "Explorá la comunidad"

                            else ->
                                "Servicios disponibles (${filteredListings.size})"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "NEXORA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.errorMessage != null) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    OutlinedCard {
                        Column(
                            modifier = Modifier.padding(
                                Espaciado.grande
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                Espaciado.mediano
                            )
                        ) {
                            Text(
                                text = if (necesitaAcceso) {
                                    "Tu comunidad te espera"
                                } else {
                                    "No pudimos cargar las ofertas"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = if (necesitaAcceso) {
                                    "Ingresá o creá tu cuenta para conectar con estudiantes emprendedores."
                                } else {
                                    state.errorMessage.orEmpty()
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (necesitaAcceso) {
                                Button(onClick = onPublishClick) {
                                    Text("Ingresar a Nexora")
                                }
                            }

                            TextButton(onClick = onRetryClick) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (
                !state.isLoading &&
                state.errorMessage == null &&
                filteredListings.isEmpty()
            ) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            Espaciado.mediano
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tienda),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (state.listings.isEmpty()) {
                                "El próximo emprendimiento puede ser el tuyo."
                            } else {
                                "No encontramos coincidencias."
                            },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = if (state.listings.isEmpty()) {
                                "Publicá tu primera oferta en Nexora."
                            } else {
                                "Probá otra búsqueda o categoría."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (state.listings.isEmpty()) {
                            Button(onClick = onPublishClick) {
                                Text("Publicar mi primera oferta")
                            }
                        }
                    }
                }
            }

            items(
                items = filteredListings,
                key = {
                    it.id
                }
            ) { listing ->
                ServiceListingCard(
                    listing = listing,
                    onClick = {
                        onListingClick(listing.id)
                    },
                    imageContent = listingImageContent
                )
            }
        }
    }
}

@Composable
fun ServiceListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageContent: @Composable (Listing, Modifier) -> Unit = { item, imageModifier ->
        DefaultListingImage(
            listing = item,
            modifier = imageModifier
        )
    }
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
        ) {
            imageContent(
                listing,
                Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = listing.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = listing.sellerName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Q${formatPrice(listing.price)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DefaultListingImage(
    listing: Listing,
    modifier: Modifier = Modifier
) {
    if (listing.imageUrl.isNotBlank()) {
        AsyncImage(
            model = listing.imageUrl,
            contentDescription = listing.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (listing.category == "Tutorías") {
                        R.drawable.ic_estudios
                    } else {
                        R.drawable.ic_tienda
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
