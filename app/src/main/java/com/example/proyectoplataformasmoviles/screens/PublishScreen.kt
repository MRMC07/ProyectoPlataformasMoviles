package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.model.ListingCategory
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
    var isCategoryExpanded by remember { mutableStateOf(false) }

    val isFinished = state.isSaved || state.isQueued
    val fieldsEnabled = !state.isSubmitting && !isFinished

    Scaffold(
        modifier = modifier,
        topBar = {
            BackButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva publicación",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                enabled = fieldsEnabled,
                isError = state.titleTouched && state.titleError != null,
                supportingText = {
                    if (state.titleTouched) {
                        state.titleError?.let { Text(it) }
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
                enabled = fieldsEnabled,
                isError = state.descriptionTouched && state.descriptionError != null,
                supportingText = {
                    if (state.descriptionTouched) {
                        state.descriptionError?.let { Text(it) }
                    }
                }
            )

            OutlinedTextField(
                value = state.price,
                onValueChange = onPriceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Precio en quetzales") },
                singleLine = true,
                enabled = fieldsEnabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.priceTouched && state.priceError != null,
                supportingText = {
                    if (state.priceTouched) {
                        state.priceError?.let { Text(it) }
                    }
                }
            )

            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box {
                OutlinedButton(
                    onClick = { isCategoryExpanded = true },
                    enabled = fieldsEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ListingCategory.fromId(state.category)?.label
                            ?: "Seleccionar categoría"
                    )
                }

                DropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = { isCategoryExpanded = false }
                ) {
                    ListingCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = {
                                onCategoryChange(category.id)
                                isCategoryExpanded = false
                            }
                        )
                    }
                }
            }

            state.notice?.let { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isSaved) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.isSaved) Icons.Filled.CheckCircle else Icons.Filled.Info,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = if (isFinished) onBackClick else onPublishClick,
                enabled = isFinished || state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    when {
                        isFinished -> "Volver al inicio"
                        state.isSubmitting -> "Publicando..."
                        else -> "Publicar"
                    }
                )
            }
        }
    }
}