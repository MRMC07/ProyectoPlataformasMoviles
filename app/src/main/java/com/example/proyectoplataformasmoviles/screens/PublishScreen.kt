package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.presentation.publish.PublishUiState

@Composable
fun PublishScreen(
    state: PublishUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPublishClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCategoryMenuExpanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Tutorías",
        "Comida",
        "Tecnología",
        "Diseño"
    )

    val canEdit = !state.isSaving && !state.isPublished

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("Volver")
            }

            Text(
                text = "Publicar",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                label = {
                    Text("Título del producto o servicio")
                },
                singleLine = true,
                isError = state.titleError != null,
                supportingText = {
                    state.titleError?.let { message ->
                        Text(message)
                    }
                }
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                label = {
                    Text("Descripción")
                },
                minLines = 3,
                isError = state.descriptionError != null,
                supportingText = {
                    state.descriptionError?.let { message ->
                        Text(message)
                    }
                }
            )

            OutlinedTextField(
                value = state.price,
                onValueChange = onPriceChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                label = {
                    Text("Precio en quetzales")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = state.priceError != null,
                supportingText = {
                    state.priceError?.let { message ->
                        Text(message)
                    }
                }
            )

            Column {
                Button(
                    onClick = {
                        isCategoryMenuExpanded = true
                    },
                    enabled = canEdit
                ) {
                    Text(
                        text = state.category.ifBlank {
                            "Seleccionar categoría"
                        }
                    )
                }

                DropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = {
                        isCategoryMenuExpanded = false
                    }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(category)
                            },
                            onClick = {
                                onCategoryChange(category)
                                isCategoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            state.categoryError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isSaving) {
                CircularProgressIndicator()
                Text("Guardando publicación…")
            }

            Button(
                onClick = onPublishClick,
                enabled = state.canPublish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar")
            }
        }
    }
}