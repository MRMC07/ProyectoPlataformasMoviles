package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.ui.theme.Espaciado
import com.example.proyectoplataformasmoviles.R

@Composable
fun OnboardingScreen(onStartRegistration: () -> Unit, onExploreAsGuest: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, modifier = modifier) { margenes ->
        Column(Modifier.fillMaxSize().padding(margenes).verticalScroll(rememberScrollState()).padding(Espaciado.amplio),
            verticalArrangement = Arrangement.spacedBy(Espaciado.amplio)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                MarcaNexora()
                Text("GUATEMALA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.mediano)) {
                Text("Talento cerca.\nPosibilidades enormes.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text("Descubrí lo que crean estudiantes como vos. Encontrá productos, ofrecé servicios y hacé crecer tu emprendimiento.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(Espaciado.amplio), verticalArrangement = Arrangement.spacedBy(Espaciado.grande)) {
                    Text("HECHO POR ESTUDIANTES", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.normal)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Espaciado.pequeno)) {
                            Icon(painterResource(R.drawable.ic_tienda), null, Modifier.size(32.dp))
                            Text("Ideas que\nse venden", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Espaciado.pequeno)) {
                            Icon(painterResource(R.drawable.ic_estudios), null, Modifier.size(32.dp))
                            Text("Talento que\nse comparte", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text("Una comunidad. Muchas universidades.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onStartRegistration, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("Crear mi cuenta", modifier = Modifier.weight(1f))
                    Icon(painterResource(R.drawable.ic_flecha), null)
                }
                OutlinedButton(onClick = onExploreAsGuest, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("Explorar Nexora")
                }
                Text("Tu próxima conexión empieza acá.", modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


