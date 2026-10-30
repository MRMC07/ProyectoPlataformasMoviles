
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.presentation.MyProductsUiState

@Composable
fun MyProductsScreen(
    state: MyProductsUiState,
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    onDeleteDraft: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text("← Regresar")
            }
        },
        bottomBar = {
            Button(
                onClick = onPublishClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("+ Crear publicación")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mis productos",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Borradores guardados en este dispositivo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                    Text("Cargando borradores...")
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.isEmpty -> {
                    Text(
                        text = "Todavía no tenés borradores guardados.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding =
                            PaddingValues(bottom = 16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.drafts,
                            key = { draft -> draft.id }
                        ) { draft ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = draft.title,
                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = draft.description,
                                        maxLines = 3
                                    )

                                    Text(
                                        text = "Q${draft.price}"
                                    )

                                    Text(
                                        text = draft.category,
                                        color =
                                            MaterialTheme.colorScheme.primary
                                    )

                                    TextButton(
                                        onClick = {
                                            onDeleteDraft(draft.id)
                                        }
                                    ) {
                                        Text("Eliminar borrador")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
