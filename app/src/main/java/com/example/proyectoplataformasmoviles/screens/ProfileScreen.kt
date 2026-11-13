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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.ui.theme.Spacing
import com.example.proyectoplataformasmoviles.ui.theme.UvgBackground
import com.example.proyectoplataformasmoviles.ui.theme.UvgBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGreenPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgSurface
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextMuted
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextSecondary
import java.util.Locale

data class UserReview(
    val author: String,
    val rating: Int,
    val comment: String,
    val date: String
)

/**
 * Pantalla correspondiente al "Perfil E." del diagrama en el pizarrón:
 * - Avatar / Foto de perfil
 * - Datos del usuario y universidad a la que pertenece
 * - Publicaciones del usuario (sin datos ficticios asignados)
 * - Reseñas recibidas reales o estado vacío explícito
 */
@Composable
fun ProfileScreen(
    userName: String,
    userRole: String,
    onBackClick: () -> Unit,
    onListingClick: (String) -> Unit,
    onPublishClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    userUniversity: String = "Universidad en Guatemala",
    userListings: List<Listing> = emptyList(),
    reviews: List<UserReview> = emptyList(),
    alVerBorradores: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Publicaciones, 1: Reseñas

    Scaffold(
        containerColor = UvgBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            // 1. Barra de Navegación Superior
            item {
                Surface(
                    color = UvgSurface,
                    border = BorderStroke(1.dp, UvgBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.Medium, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = onBackClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = UvgGreenPrimary)
                        ) {
                            Text("← Volver al Inicio", fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = onLogoutClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
                        ) {
                            Text("Cerrar Sesión", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                TextButton(onClick = alVerBorradores, modifier = Modifier.padding(horizontal = Spacing.Large)) {
                    Text("Borradores de este dispositivo")
                }
            }
            // 2. Cabecera del Perfil ("Perfil E." en el pizarrón)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Large),
                    color = UvgSurface,
                    shape = RoundedCornerShape(Spacing.CornerLarge),
                    border = BorderStroke(1.dp, UvgBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Large),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        // Foto de Perfil / Tomar Foto
                        Box(
                            modifier = Modifier
                                .size(Spacing.AvatarSizeLarge)
                                .clip(CircleShape)
                                .background(UvgGreenLight)
                                .border(2.dp, UvgGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = UvgGreenDark
                            )
                        }

                        // Nombre y Rol
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = UvgTextPrimary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = UvgGreenLight,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (userRole.contains("Emprendedor", ignoreCase = true)) "💡 Emprendedor Universitario" else "🎒 Comprador / Estudiante",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UvgGreenDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Calificación real sin inventar 32 reseñas ficticias (Prioridad 5)
                            Surface(
                                color = UvgGoldLight,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, UvgGoldDark.copy(alpha = 0.2f))
                            ) {
                                val ratingText = if (reviews.isNotEmpty()) {
                                    val avg = reviews.map { it.rating }.average()
                                    "★ ${String.format(Locale.US, "%.1f", avg)} (${reviews.size} reseñas)"
                                } else {
                                    "Perfil nuevo (0 reseñas)"
                                }
                                Text(
                                    text = ratingText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UvgGoldDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Universidad que pertenece
                        Text(
                            text = "🏛️ $userUniversity",
                            fontSize = 12.sp,
                            color = UvgTextSecondary
                        )

                        if (userRole.contains("Emprendedor", ignoreCase = true)) {
                            Button(
                                onClick = onPublishClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UvgGreenPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(Spacing.CornerMedium),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Spacing.Small)
                            ) {
                                Text("+ Publicar nuevo servicio", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 3. Pestañas de Secciones: "Publicaciones" y "Reseñas"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Large),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileTabPill(
                        title = "Mis Publicaciones (${userListings.size})",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )

                    ProfileTabPill(
                        title = "Reseñas Recibidas (${reviews.size})",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Contenido según pestaña (Con estados vacíos explícitos, sin invenciones)
            if (selectedTab == 0) {
                if (userListings.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.Large),
                            color = UvgSurface,
                            shape = RoundedCornerShape(Spacing.CornerLarge),
                            border = BorderStroke(1.dp, UvgBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.Huge),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                            ) {
                                Text("📦", fontSize = 32.sp)
                                Text(
                                    text = "Sin publicaciones activas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = UvgTextPrimary
                                )
                                Text(
                                    text = "Aún no has registrado productos o servicios con esta cuenta.",
                                    fontSize = 12.sp,
                                    color = UvgTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(Spacing.Small))
                                Button(
                                    onClick = onPublishClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = UvgGreenPrimary),
                                    shape = RoundedCornerShape(Spacing.CornerMedium)
                                ) {
                                    Text("+ Crear mi primera publicación", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(userListings, key = { it.id }) { listing ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.Large)
                                .clip(RoundedCornerShape(Spacing.CornerMedium))
                                .clickable { onListingClick(listing.id) },
                            color = UvgSurface,
                            shape = RoundedCornerShape(Spacing.CornerMedium),
                            border = BorderStroke(1.dp, UvgBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(UvgGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📚", fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(Spacing.Medium))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = listing.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = UvgTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${listing.category} • ${listing.deliveryMode}",
                                        fontSize = 11.sp,
                                        color = UvgTextSecondary
                                    )
                                }

                                Text(
                                    text = "Q${String.format(Locale.US, "%.2f", listing.price)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = UvgGreenDark
                                )
                            }
                        }
                    }
                }
            } else {
                if (reviews.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.Large),
                            color = UvgSurface,
                            shape = RoundedCornerShape(Spacing.CornerLarge),
                            border = BorderStroke(1.dp, UvgBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.Huge),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                            ) {
                                Text("⭐", fontSize = 32.sp)
                                Text(
                                    text = "Sin reseñas recibidas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = UvgTextPrimary
                                )
                                Text(
                                    text = "Las calificaciones de otros estudiantes aparecerán aquí una vez completen acuerdos de servicio.",
                                    fontSize = 12.sp,
                                    color = UvgTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(reviews) { review ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.Large),
                            color = UvgSurface,
                            shape = RoundedCornerShape(Spacing.CornerMedium),
                            border = BorderStroke(1.dp, UvgBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.Medium),
                                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = review.author,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = UvgTextPrimary
                                    )
                                    Text(
                                        text = review.date,
                                        fontSize = 11.sp,
                                        color = UvgTextMuted
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(review.rating) {
                                        Text("★", color = UvgGoldPrimary, fontSize = 14.sp)
                                    }
                                }

                                Text(
                                    text = review.comment,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UvgTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.Large))
            }
        }
    }
}

@Composable
private fun ProfileTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.CornerMedium))
            .clickable(onClick = onClick),
        color = if (isSelected) UvgGreenPrimary else UvgSurface,
        shape = RoundedCornerShape(Spacing.CornerMedium),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) UvgGreenPrimary else UvgBorder
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else UvgTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
