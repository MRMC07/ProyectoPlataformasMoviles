
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyProductsScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Mis productos",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                "Administra tus productos y servicios."
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Todavía no hay publicaciones para mostrar.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        "Tus publicaciones aparecerán aquí " +
                                "cuando se conecte tu cuenta.",
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onPublishClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Crear publicación")
            }
        }
    }
}
