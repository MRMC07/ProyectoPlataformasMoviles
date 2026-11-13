package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectoplataformasmoviles.presentation.publish.PublishUiState
import com.example.proyectoplataformasmoviles.ui.theme.UvgBackground
import com.example.proyectoplataformasmoviles.ui.theme.UvgBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgSurface
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextMuted
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextSecondary

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
        "Tecnología",
        "Diseño",
        "Comida",
        "Consultoría"
    )

    val canEdit = !state.isSaving && !state.isPublished

    Scaffold(
        containerColor = UvgBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navegación Superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = UvgGreenPrimary)
                ) {
                    Text("← Regresar", fontWeight = FontWeight.SemiBold)
                }

                Surface(
                    color = UvgGoldLight,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Emprendé en Nexora",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UvgGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Publicar nuevo servicio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = UvgGreenDark
                )
                Text(
                    text = "Ofrecé tus productos y servicios a estudiantes de todas las universidades de Guatemala.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UvgTextSecondary
                )
            }

            // Título del Servicio
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Título de tu servicio o producto",
                    style = MaterialTheme.typography.labelLarge,
                    color = UvgTextPrimary
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canEdit,
                    placeholder = {
                        Text("Ej. Tutoría de Álgebra Lineal y Cálculo II", color = UvgTextMuted)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = state.titleTouched && state.titleError != null,
                    supportingText = {
                        if (state.titleTouched) {
                            state.titleError?.let { Text(it) }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UvgGreenPrimary,
                        unfocusedBorderColor = UvgBorder,
                        focusedContainerColor = UvgSurface,
                        unfocusedContainerColor = UvgSurface
                    )
                )
            }

            // Descripción Detallada
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Descripción del servicio",
                    style = MaterialTheme.typography.labelLarge,
                    color = UvgTextPrimary
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canEdit,
                    placeholder = {
                        Text("Explica qué incluye, metodología o temas que cubres...", color = UvgTextMuted)
                    },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    isError = state.descriptionTouched && state.descriptionError != null,
                    supportingText = {
                        if (state.descriptionTouched) {
                            state.descriptionError?.let { Text(it) }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UvgGreenPrimary,
                        unfocusedBorderColor = UvgBorder,
                        focusedContainerColor = UvgSurface,
                        unfocusedContainerColor = UvgSurface
                    )
                )
            }

            // Precio en Quetzales
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Precio en Quetzales (Q)",
                    style = MaterialTheme.typography.labelLarge,
                    color = UvgTextPrimary
                )
                OutlinedTextField(
                    value = state.price,
                    onValueChange = onPriceChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canEdit,
                    prefix = { Text("Q ", fontWeight = FontWeight.Bold, color = UvgGreenDark) },
                    placeholder = { Text("75.00", color = UvgTextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    isError = state.priceTouched && state.priceError != null,
                    supportingText = {
                        if (state.priceTouched) {
                            state.priceError?.let { Text(it) }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UvgGreenPrimary,
                        unfocusedBorderColor = UvgBorder,
                        focusedContainerColor = UvgSurface,
                        unfocusedContainerColor = UvgSurface
                    )
                )
            }

            // Categoría
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Categoría de servicio",
                    style = MaterialTheme.typography.labelLarge,
                    color = UvgTextPrimary
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canEdit) { isCategoryMenuExpanded = true },
                        color = UvgSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, UvgBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.category.ifBlank { "Selecciona una categoría..." },
                                color = if (state.category.isNotBlank()) UvgTextPrimary else UvgTextMuted,
                                fontSize = 14.sp
                            )
                            Text("▼", fontSize = 12.sp, color = UvgGreenPrimary)
                        }
                    }

                    DropdownMenu(
                        expanded = isCategoryMenuExpanded,
                        onDismissRequest = { isCategoryMenuExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    onCategoryChange(category)
                                    isCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (state.categoryTouched) {
                    state.categoryError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Aviso para la comunidad universitaria
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = UvgGreenLight,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, UvgGreenBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tu publicación será visible para la comunidad de Nexora. Acordá con la otra persona un lugar de entrega o una modalidad virtual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = UvgGreenDark,
                        lineHeight = 16.sp
                    )
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            if (state.isSaving) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = UvgGreenPrimary, modifier = Modifier.size(20.dp))
                    Text("Guardando publicación en Nexora...", fontSize = 13.sp, color = UvgTextSecondary)
                }
            }

            // Botón de Publicar
            Button(
                onClick = onPublishClick,
                enabled = state.canPublish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UvgGreenPrimary,
                    disabledContainerColor = UvgGreenPrimary.copy(alpha = 0.35f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (state.isSaving) "Publicando..." else "Publicar en Nexora",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
