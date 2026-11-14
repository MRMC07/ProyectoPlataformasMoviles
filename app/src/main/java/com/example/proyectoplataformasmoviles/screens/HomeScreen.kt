package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.proyectoplataformasmoviles.ui.theme.Espaciado
import com.example.proyectoplataformasmoviles.R
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.formatearImporte
import com.example.proyectoplataformasmoviles.presentation.home.HomeUiState

@Composable
fun HomeScreen(state: HomeUiState, onListingClick: (String) -> Unit, onPublishClick: () -> Unit,
    onRetryClick: () -> Unit, modifier: Modifier = Modifier, userName: String = "Visitante",
    userRole: String = "Estudiante", onProfileClick: () -> Unit = {}, necesitaAcceso: Boolean = false) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Tutorías", "Tecnología", "Diseño", "Comida", "Consultoría")
    val resultados = state.listings.filter {
        (categoria == "Todos" || it.category.equals(categoria, true)) &&
            (busqueda.isBlank() || "${it.title} ${it.description} ${it.sellerName}".contains(busqueda.trim(), true))
    }
    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(painterResource(R.drawable.ic_inicio), null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = onPublishClick, icon = { Icon(painterResource(R.drawable.ic_publicar), null) }, label = { Text("Publicar") })
                NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(painterResource(R.drawable.ic_perfil), null) }, label = { Text("Perfil") })
            }
        }) { margenes ->
        LazyVerticalGrid(columns = GridCells.Adaptive(160.dp), modifier = Modifier.fillMaxSize().padding(margenes),
            contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(Espaciado.mediano),
            verticalArrangement = Arrangement.spacedBy(Espaciado.normal)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    MarcaNexora()
                    FilledTonalIconButton(onClick = onProfileClick) { Icon(painterResource(R.drawable.ic_perfil), "Abrir mi perfil") }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.pequeno)) {
                    Text("Hola, $userName", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Descubrí el talento\nde tu comunidad.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Estudiantes de todas las universidades de Guatemala.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(busqueda, { busqueda = it }, singleLine = true,
                    placeholder = { Text("¿Qué estás buscando?") }, leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), null) },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Espaciado.pequeno)) {
                    categorias.forEach { opcion ->
                        FilterChip(selected = categoria == opcion, onClick = { categoria = opcion }, label = { Text(opcion) })
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (state.isLoading) "Cargando ofertas" else if (state.errorMessage != null) "Explorá la comunidad" else "Servicios Disponibles (${resultados.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("NEXORA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (state.errorMessage != null) item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedCard {
                    Column(Modifier.padding(Espaciado.grande), verticalArrangement = Arrangement.spacedBy(Espaciado.mediano)) {
                        Text(if (necesitaAcceso) "Tu comunidad te espera" else "No pudimos cargar las ofertas", style = MaterialTheme.typography.titleMedium)
                        Text(if (necesitaAcceso) "Ingresá o creá tu cuenta para conectar con estudiantes emprendedores." else state.errorMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (necesitaAcceso) Button(onClick = onPublishClick) { Text("Ingresar a Nexora") }
                        TextButton(onClick = onRetryClick) { Text("Reintentar") }
                    }
                }
            }
            if (state.isLoading) item(span = { GridItemSpan(maxLineSpan) }) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (!state.isLoading && state.errorMessage == null && resultados.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Espaciado.mediano)) {
                    Icon(painterResource(R.drawable.ic_tienda), null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(if (state.listings.isEmpty()) "El próximo emprendimiento puede ser el tuyo." else "No encontramos coincidencias.",
                        style = MaterialTheme.typography.titleMedium)
                    Text(if (state.listings.isEmpty()) "Publicá tu primera oferta en Nexora." else "Probá otra búsqueda o categoría.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.listings.isEmpty()) Button(onClick = onPublishClick) { Text("Publicar mi primera oferta") }
                }
            }
            items(resultados, key = { it.id }) { oferta ->
                ServiceListingCard(oferta, { onListingClick(oferta.id) })
            }
        }
    }
}

@Composable
fun ServiceListingCard(listing: Listing, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().height(128.dp)) {
            if (listing.imageUrl.isNotBlank()) AsyncImage(model = listing.imageUrl, contentDescription = listing.title,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(painterResource(if (listing.category == "Tutorías") R.drawable.ic_estudios else R.drawable.ic_tienda), null,
                    Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(listing.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
            Text(listing.sellerName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Q${formatearImporte(listing.price)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}


