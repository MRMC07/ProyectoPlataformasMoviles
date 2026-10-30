
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.presentation.PublishUiState

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
    var isCategoryExpanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Tutorías",
        "Comida",
        "Tecnología",
        "Diseño"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 8.dp
                )
            ) {
                Text("← Regresar")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva publicación",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Publica tu producto o servicio",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Completa los datos de tu publicación.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                singleLine = true,
                enabled = !state.isSubmitting,
                isError = state.titleTouched &&
                        state.titleError != null,
                supportingText = {
                    if (state.titleTouched) {
                        state.titleError?.let { error ->
                            Text(error)
                        }
                    }
                }
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
                minLines = 3,
                maxLines = 5,
                enabled = !state.isSubmitting,
                isError = state.descriptionTouched &&
                        state.descriptionError != null,
                supportingText = {
                    if (state.descriptionTouched) {
                        state.descriptionError?.let { error ->
                            Text(error)
                        }
                    }
                }
            )

            OutlinedTextField(
                value = state.price,
                onValueChange = onPriceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Precio en quetzales") },
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = state.priceTouched &&
                        state.priceError != null,
                supportingText = {
                    if (state.priceTouched) {
                        state.priceError?.let { error ->
                            Text(error)
                        }
                    }
                }
            )

            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = { isCategoryExpanded = true },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        state.category.ifBlank {
                            "Seleccionar categoría"
                        }
                    )
                }

                DropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = {
                        isCategoryExpanded = false
                    }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategoryChange(category)
                                isCategoryExpanded = false
                            }
                        )
                    }
                }
            }

            state.notice?.let { notice ->
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onPublishClick,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (state.isSubmitting) {
                        "Procesando..."
                    } else {
                        "Publicar"
                    }
                )
            }

            Text(
                text = "El guardado de publicaciones " +
                        "requiere conectar el repositorio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
