package com.example.proyectoplataformasmoviles.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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

data class Publicacion(
    val nombre: String,
    val precio: String,
    val categoria: String,
    val vendedor: String,
    val imagenUrl: String
)

@Composable
fun InicioScreen(
    onPublicacionClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyProductsClick: () -> Unit,
    onProfileClick: () -> Unit
) {

    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    val publicaciones = listOf(
        Publicacion(
            nombre = "Tutorías de Matemática",
            precio = "Q75 / hora",
            categoria = "Tutorías",
            vendedor = "Vendedor verificado",
            imagenUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=600&q=80"
        ),
        Publicacion(
            nombre = "Brownies caseros",
            precio = "Q25",
            categoria = "Comida",
            vendedor = "Vendedor verificado",
            imagenUrl = "https://images.unsplash.com/photo-1607920591413-4ec007e70023?auto=format&fit=crop&w=600&q=80"
        ),
        Publicacion(
            nombre = "Soporte técnico para computadoras",
            precio = "Q100",
            categoria = "Tecnología",
            vendedor = "Vendedor verificado",
            imagenUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=600&q=80"
        ),
        Publicacion(
            nombre = "Diseño gráfico para redes sociales",
            precio = "Q150",
            categoria = "Diseño",
            vendedor = "Vendedor verificado",
            imagenUrl = "https://images.unsplash.com/photo-1561070791-2526d30994b5?auto=format&fit=crop&w=600&q=80"
        )
    )

    val publicacionesFiltradas = publicaciones.filter { publicacion ->

        val coincideBusqueda = publicacion.nombre.contains(
            busqueda,
            ignoreCase = true
        )

        val coincideCategoria =
            categoriaSeleccionada == "Todos" ||
                    publicacion.categoria == categoriaSeleccionada

        coincideBusqueda && coincideCategoria
    }

    Scaffold(
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
            BarraInferior(
                onMyProductsClick = onMyProductsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(innerPadding)
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
                value = busqueda,
                onValueChange = {
                    busqueda = it
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

                CategoriaItem(
                    nombre = "Todos",
                    simbolo = "★",
                    seleccionada = categoriaSeleccionada == "Todos",
                    onClick = {
                        categoriaSeleccionada = "Todos"
                    }
                )

                CategoriaItem(
                    nombre = "Tutorías",
                    simbolo = "📚",
                    seleccionada = categoriaSeleccionada == "Tutorías",
                    onClick = {
                        categoriaSeleccionada = "Tutorías"
                    }
                )

                CategoriaItem(
                    nombre = "Comida",
                    simbolo = "🍰",
                    seleccionada = categoriaSeleccionada == "Comida",
                    onClick = {
                        categoriaSeleccionada = "Comida"
                    }
                )

                CategoriaItem(
                    nombre = "Tecnología",
                    simbolo = "💻",
                    seleccionada = categoriaSeleccionada == "Tecnología",
                    onClick = {
                        categoriaSeleccionada = "Tecnología"
                    }
                )

                CategoriaItem(
                    nombre = "Diseño",
                    simbolo = "🎨",
                    seleccionada = categoriaSeleccionada == "Diseño",
                    onClick = {
                        categoriaSeleccionada = "Diseño"
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

                items(publicacionesFiltradas) { publicacion ->

                    PublicacionCard(
                        publicacion = publicacion,
                        onClick = onPublicacionClick
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriaItem(
    nombre: String,
    simbolo: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (seleccionada) {
                        Color(0xFF17345F)
                    } else {
                        Color(0xFFE4EBF3)
                    },
                contentColor =
                    if (seleccionada) {
                        Color.White
                    } else {
                        Color(0xFF25364A)
                    }
            )
        ) {

            Text(
                text = simbolo,
                fontSize = 19.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = nombre,
            fontSize = 10.sp
        )
    }
}

@Composable
fun PublicacionCard(
    publicacion: Publicacion,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
                    model = publicacion.imagenUrl,
                    contentDescription = publicacion.nombre,
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
                    text = publicacion.nombre,
                    fontSize = 13.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = publicacion.precio,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "✓ ${publicacion.vendedor}",
                    fontSize = 10.sp,
                    color = Color(0xFF238B57)
                )
            }
        }
    }
}


@Composable
fun BarraInferior(
    onMyProductsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarraItem(
            simbolo = "⌂",
            texto = "Inicio"
        )

        BarraItem(
            simbolo = "⌕",
            texto = "Búsqueda"
        )

        BarraItem(
            simbolo = "▣",
            texto = "Mis Productos",
            onClick = onMyProductsClick
        )

        BarraItem(
            simbolo = "●",
            texto = "Perfil",
            onClick = onProfileClick
        )
    }
}

@Composable
fun BarraItem(
    simbolo: String,
    texto: String,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = simbolo,
            fontSize = 18.sp,
            color = Color(0xFF17345F)
        )

        Text(
            text = texto,
            fontSize = 9.sp
        )
    }
}


@Composable
fun BarraItem(
    simbolo: String,
    texto: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = simbolo,
            fontSize = 18.sp,
            color = Color(0xFF17345F)
        )

        Text(
            text = texto,
            fontSize = 9.sp
        )
    }
}