package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectoplataformasmoviles.ui.theme.Spacing
import com.example.proyectoplataformasmoviles.ui.theme.UvgBackground
import com.example.proyectoplataformasmoviles.ui.theme.UvgBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgSurface
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextMuted
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextSecondary

/**
 * Pantalla / Diálogo de Reseña (Boceto "C: Reseña" del pizarrón):
 * - Califica tu experiencia
 * - Nombre de servicio / emprendedor
 * - Estrellas interactivas (1 a 5)
 * - Comentario
 * - Botón de Enviar
 */
@Composable
fun ReviewDialog(
    serviceTitle: String,
    sellerName: String,
    onDismissRequest: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = UvgSurface,
        shape = RoundedCornerShape(Spacing.CornerLarge),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = UvgGoldLight,
                    shape = RoundedCornerShape(Spacing.CornerMedium)
                ) {
                    Text(
                        text = "⭐ Reseña del Servicio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UvgGoldDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Small))

                Text(
                    text = "Califica tu experiencia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UvgGreenDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tu valoración ayuda a la comunidad universitaria a conocer el trabajo del emprendedor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UvgTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // Servicio y Emprendedor evaluado
                Surface(
                    color = UvgBackground,
                    shape = RoundedCornerShape(Spacing.CornerMedium),
                    border = BorderStroke(1.dp, UvgBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = serviceTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = UvgTextPrimary
                        )
                        Text(
                            text = "Emprendedor: $sellerName",
                            fontSize = 12.sp,
                            color = UvgTextSecondary
                        )
                    }
                }

                // Selector de 5 Estrellas Interactivas
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (star in 1..5) {
                            val isSelected = star <= selectedStars
                            Text(
                                text = if (isSelected) "★" else "☆",
                                fontSize = 34.sp,
                                color = if (isSelected) UvgGoldPrimary else UvgBorder,
                                modifier = Modifier
                                    .clickable { selectedStars = star }
                                    .padding(2.dp)
                            )
                        }
                    }

                    Text(
                        text = when (selectedStars) {
                            5 -> "Excelente servicio ★★★★★"
                            4 -> "Muy buen trabajo ★★★★☆"
                            3 -> "Cumplió lo acordado ★★★☆☆"
                            2 -> "Puede mejorar ★★☆☆☆"
                            else -> "No satisfecho ★☆☆☆☆"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UvgGoldDark
                    )
                }

                // Comentario de retroalimentación
                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    placeholder = {
                        Text(
                            "Escribe un breve comentario sobre la puntualidad, calidad o trato recibido...",
                            fontSize = 12.sp,
                            color = UvgTextMuted
                        )
                    },
                    shape = RoundedCornerShape(Spacing.CornerMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UvgGreenPrimary,
                        unfocusedBorderColor = UvgBorder,
                        focusedContainerColor = UvgBackground,
                        unfocusedContainerColor = UvgBackground
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitReview(selectedStars, reviewComment)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = UvgGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(Spacing.CornerMedium),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Publicar Reseña", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = UvgTextSecondary)
            ) {
                Text("Cerrar")
            }
        }
    )
}
