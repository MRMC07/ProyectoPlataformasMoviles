package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.defaultUvgListings
import com.example.proyectoplataformasmoviles.ui.theme.Spacing
import com.example.proyectoplataformasmoviles.ui.theme.UvgBackground
import com.example.proyectoplataformasmoviles.ui.theme.UvgBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgSurface
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextSecondary
import java.util.Locale

@Composable
fun ServiceOrderScreen(
    listingId: String,
    onBackClick: () -> Unit,
    onProceedToChat: (modality: String, initialMessage: String, schedule: String) -> Unit,
    modifier: Modifier = Modifier,
    listings: List<Listing> = defaultUvgListings
) {
    val listing = listings.find { it.id == listingId }

    if (listing == null) {
        // Manejo explícito sin sustituciones ficticias
        Scaffold(
            containerColor = UvgBackground,
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.Large),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = UvgSurface,
                    shape = RoundedCornerShape(Spacing.CornerLarge),
                    border = BorderStroke(1.dp, UvgBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.Huge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                    ) {
                        Text("🔍", fontSize = 36.sp)
                        Text(
                            text = "Publicación no encontrada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UvgTextPrimary
                        )
                        Text(
                            text = "No se pudo cargar la información para configurar la orden.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UvgTextSecondary
                        )
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = UvgGreenPrimary),
                            shape = RoundedCornerShape(Spacing.CornerMedium)
                        ) {
                            Text("← Volver al catálogo")
                        }
                    }
                }
            }
        }
        return
    }

    var selectedModality by remember(listingId) {
        mutableStateOf(if (listing.deliveryMode.contains("CIT", ignoreCase = true) || listing.deliveryMode.contains("Campus", ignoreCase = true)) "Presencial en Campus" else "Virtual (Meet)")
    }
    var selectedSchedule by remember(listingId) { mutableStateOf("Martes o Jueves por la tarde") }
    var notes by remember(listingId) {
        mutableStateOf("¡Hola ${listing.sellerName}! Me interesa tu servicio de \"${listing.title}\". ¿Tienes disponibilidad para coordinar esta semana?")
    }

    val scheduleOptions = listOf(
        "Martes o Jueves por la tarde",
        "Lunes o Miércoles entre clases",
        "Viernes horario flexible",
        "Fin de semana (virtual)"
    )

    Scaffold(
        containerColor = UvgBackground,
        modifier = modifier,
        bottomBar = {
            Surface(
                color = UvgSurface,
                border = BorderStroke(1.dp, UvgBorder),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Large, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total a acordar", fontSize = 11.sp, color = UvgTextSecondary)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("Q", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = UvgGreenDark)
                            Text(
                                text = String.format(Locale.US, "%.2f", listing.price),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = UvgGreenDark
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onProceedToChat(selectedModality, notes.trim(), selectedSchedule)
                        },
                        modifier = Modifier.height(Spacing.MinTouchTarget),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UvgGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(Spacing.CornerMedium)
                    ) {
                        Text(
                            text = "Revisar consulta →",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.Large, vertical = Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            // Barra Superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = UvgGreenPrimary)
                ) {
                    Text("← Cambiar servicio", fontWeight = FontWeight.SemiBold)
                }

                Surface(
                    color = UvgGreenLight,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Configuración de Solicitud",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UvgGreenDark,
                        modifier = Modifier.padding(horizontal = Spacing.Small, vertical = 4.dp)
                    )
                }
            }

            // Título
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)) {
                Text(
                    text = "Prepará tu consulta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = UvgTextPrimary
                )
                Text(
                    text = "Proponé una modalidad y un horario para consultar al emprendedor por correo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UvgTextSecondary,
                    lineHeight = 20.sp
                )
            }

            // Tarjeta Resumen
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = UvgSurface,
                shape = RoundedCornerShape(Spacing.CornerLarge),
                border = BorderStroke(1.dp, UvgBorder)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(Spacing.CornerMedium))
                            .background(UvgGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤝", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(Spacing.Medium))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = UvgTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Por ${listing.sellerName} • ${listing.faculty}",
                            fontSize = 12.sp,
                            color = UvgTextSecondary
                        )
                    }

                    Text(
                        text = "Q${String.format(Locale.US, "%.2f", listing.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = UvgGreenDark
                    )
                }
            }

            // 1. Selector de Modalidad
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                Text(
                    text = "1. Modalidad de entrega o sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UvgTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModalityOptionCard(
                        title = "Presencial en Campus",
                        subtitle = "Lugar a convenir entre ambas personas",
                        icon = "🏛️",
                        isSelected = selectedModality == "Presencial en Campus",
                        onClick = { selectedModality = "Presencial en Campus" },
                        modifier = Modifier.weight(1f)
                    )

                    ModalityOptionCard(
                        title = "Virtual en Línea",
                        subtitle = "Google Meet o Zoom",
                        icon = "💻",
                        isSelected = selectedModality == "Virtual (Meet)",
                        onClick = { selectedModality = "Virtual (Meet)" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Horario Estimado Sugerido
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                Text(
                    text = "2. Horario sugerido para coordinar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UvgTextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    scheduleOptions.forEach { scheduleText ->
                        val isSelected = selectedSchedule == scheduleText
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Spacing.CornerMedium))
                                .clickable { selectedSchedule = scheduleText },
                            color = if (isSelected) UvgGreenLight else UvgSurface,
                            shape = RoundedCornerShape(Spacing.CornerMedium),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) UvgGreenPrimary else UvgBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) UvgGreenPrimary else UvgBorder,
                                            shape = CircleShape
                                        )
                                        .background(if (isSelected) UvgGreenPrimary else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = scheduleText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) UvgGreenDark else UvgTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Mensaje Inicial
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                Text(
                    text = "3. Mensaje inicial para el emprendedor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UvgTextPrimary
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(Spacing.CornerMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UvgGreenPrimary,
                        unfocusedBorderColor = UvgBorder,
                        focusedContainerColor = UvgSurface,
                        unfocusedContainerColor = UvgSurface
                    )
                )
            }

            // Nota de transparencia sobre estado local
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = UvgGoldLight,
                shape = RoundedCornerShape(Spacing.CornerMedium),
                border = BorderStroke(1.dp, UvgGoldDark.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Consulta sin compromiso",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = UvgGoldDark
                        )
                        Text(
                            text = "En el siguiente paso podrás revisar estos datos y abrir tu aplicación de correo para enviarlos. No se confirma una compra.",
                            fontSize = 11.sp,
                            color = UvgTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Medium))
        }
    }
}

@Composable
private fun ModalityOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.CornerMedium))
            .clickable(onClick = onClick),
        color = if (isSelected) UvgGreenLight else UvgSurface,
        shape = RoundedCornerShape(Spacing.CornerMedium),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) UvgGreenPrimary else UvgBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) UvgGreenDark else UvgTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = UvgTextSecondary
            )
        }
    }
}
