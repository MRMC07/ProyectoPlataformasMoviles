
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onMyProductsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TextButton(onClick = onBackClick) {
                Text("← Regresar")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Mi perfil",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "👤",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        "Usuario",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        "La información de tu cuenta " +
                                "estará disponible al conectar el perfil.",
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onMyProductsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mis productos")
            }
        }
    }
}
