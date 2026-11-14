package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.ui.theme.Spacing
import com.example.proyectoplataformasmoviles.ui.theme.UvgBackground
import com.example.proyectoplataformasmoviles.ui.theme.UvgBorder
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldDark
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldLight
import com.example.proyectoplataformasmoviles.ui.theme.UvgGoldPrimary
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

@Composable
fun ProfileScreen(
    userName: String,
    onBackClick: () -> Unit,
    onListingClick: (String) -> Unit,
    onPublishClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    userUniversity: String = "Universidad sin indicar",
    userListings: List<Listing> = emptyList(),
    reviews: List<UserReview> = emptyList(),
    onDraftsClick: () -> Unit = {},
    photoBase64: String = "",
    description: String = "",
    onEditClick: () -> Unit = {},
    canEdit: Boolean = false,
    statusContent: @Composable () -> Unit = {}
) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = modifier,
        containerColor = UvgBackground
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(
                Spacing.Medium
            )
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = UvgSurface,
                    border = BorderStroke(1.dp, UvgBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Spacing.Medium,
                                vertical = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = onBackClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = UvgGreenPrimary
                            )
                        ) {
                            Text(
                                text = "← Volver al inicio",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = onLogoutClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFBA1A1A)
                            )
                        ) {
                            Text("Cerrar sesión")
                        }
                    }
                }
            }

            item {
                TextButton(
                    onClick = onDraftsClick,
                    modifier = Modifier.padding(
                        horizontal = Spacing.Large
                    )
                ) {
                    Text("Borradores de este dispositivo")
                }
            }

            item {
                statusContent()
            }

            item {
                ProfileSectionCard(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(Spacing.AvatarSizeLarge)
                            .clip(CircleShape)
                            .background(UvgGreenLight)
                            .border(
                                width = 2.dp,
                                color = UvgGreenPrimary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatar(
                            photoBase64 = photoBase64,
                            name = userName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = UvgTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        color = UvgGreenLight,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Estudiante · Compra y publica",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = UvgGreenDark,
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        color = UvgGoldLight,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        val ratingText = if (reviews.isNotEmpty()) {
                            val averageRating = reviews
                                .map { it.rating }
                                .average()

                            "★ ${
                                String.format(
                                    Locale.US,
                                    "%.1f",
                                    averageRating
                                )
                            } (${reviews.size} reseñas)"
                        } else {
                            "Reseñas pendientes"
                        }

                        Text(
                            text = ratingText,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = UvgGoldDark,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = userUniversity,
                        fontSize = 13.sp,
                        color = UvgTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = UvgTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    TextButton(
                        onClick = onEditClick,
                        enabled = canEdit
                    ) {
                        Text("Editar perfil")
                    }

                    Button(
                        onClick = onPublishClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UvgGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(
                            Spacing.CornerMedium
                        )
                    ) {
                        Text(
                            text = "+ Publicar nuevo servicio",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Large),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {
                    ProfileTabPill(
                        title = "Mis publicaciones (${userListings.size})",
                        isSelected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ProfileTabPill(
                        title = "Reseñas",
                        isSelected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedTab == 0) {
                if (userListings.isEmpty()) {
                    item {
                        ProfileSectionCard(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sin publicaciones",
                                fontWeight = FontWeight.Bold,
                                color = UvgTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Aún no has registrado productos o servicios con esta cuenta.",
                                color = UvgTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = onPublishClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UvgGreenPrimary
                                )
                            ) {
                                Text("Crear mi primera publicación")
                            }
                        }
                    }
                } else {
                    items(
                        items = userListings,
                        key = { it.id }
                    ) { listing ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Spacing.Large
                                )
                                .clip(
                                    RoundedCornerShape(
                                        Spacing.CornerMedium
                                    )
                                )
                                .clickable {
                                    onListingClick(listing.id)
                                },
                            color = UvgSurface,
                            shape = RoundedCornerShape(
                                Spacing.CornerMedium
                            ),
                            border = BorderStroke(
                                1.dp,
                                UvgBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    Spacing.Medium
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                10.dp
                                            )
                                        )
                                        .background(UvgGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📦",
                                        fontSize = 22.sp
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.width(
                                        Spacing.Medium
                                    )
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = listing.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = UvgTextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${listing.category} · ${listing.deliveryMode}",
                                        fontSize = 11.sp,
                                        color = UvgTextSecondary
                                    )

                                    Text(
                                        text = if (listing.isActive) {
                                            "Activa"
                                        } else {
                                            "Retirada"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (listing.isActive) {
                                            UvgGreenDark
                                        } else {
                                            UvgTextSecondary
                                        }
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.width(
                                        Spacing.Small
                                    )
                                )

                                Text(
                                    text = "Q${
                                        String.format(
                                            Locale.US,
                                            "%.2f",
                                            listing.price
                                        )
                                    }",
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
                        ProfileSectionCard(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Reseñas pendientes",
                                fontWeight = FontWeight.Bold,
                                color = UvgTextPrimary
                            )

                            Text(
                                text = "La función de reseñas todavía no está disponible.",
                                color = UvgTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(reviews) { review ->
                        ProfileSectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = review.author,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    color = UvgTextPrimary
                                )

                                Text(
                                    text = review.date,
                                    fontSize = 11.sp,
                                    color = UvgTextMuted
                                )
                            }

                            Row {
                                repeat(review.rating.coerceIn(0, 5)) {
                                    Text(
                                        text = "★",
                                        color = UvgGoldPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Text(
                                text = review.comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = UvgTextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(
                        Spacing.Large
                    )
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
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
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(
                Spacing.Small
            ),
            content = content
        )
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
            .clip(
                RoundedCornerShape(
                    Spacing.CornerMedium
                )
            )
            .clickable(onClick = onClick),
        color = if (isSelected) {
            UvgGreenPrimary
        } else {
            UvgSurface
        },
        shape = RoundedCornerShape(
            Spacing.CornerMedium
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                UvgGreenPrimary
            } else {
                UvgBorder
            }
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            fontSize = 12.sp,
            fontWeight = if (isSelected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (isSelected) {
                Color.White
            } else {
                UvgTextSecondary
            },
            textAlign = TextAlign.Center
        )
    }
}