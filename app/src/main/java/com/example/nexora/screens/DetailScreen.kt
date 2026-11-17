package com.example.nexora.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.nexora.domain.model.Listing
import com.example.nexora.presentation.detail.DetailUiState
import com.example.nexora.ui.theme.Spacing
import java.util.Locale

@Composable
fun DetailScreen(
    state: DetailUiState,
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canEdit: Boolean = false,
    onEditClick: () -> Unit = {},
    managementContent: @Composable () -> Unit = {},
    listingImageContent: @Composable (Listing, Modifier) -> Unit = { listing, imageModifier ->
        AsyncImage(
            model = listing.imageUrl,
            contentDescription = listing.title,
            modifier = imageModifier,
            contentScale = ContentScale.Crop
        )
    },
    reviewContent: @Composable () -> Unit = {}
) {
    val listing: Listing? = state.listing
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
        bottomBar = {
            if (
                !keyboardVisible &&
                listing != null &&
                !state.isLoading &&
                state.errorMessage == null &&
                (canEdit || listing.isActive)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Spacing.Large,
                                vertical = 14.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Precio del servicio",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "Q",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.2f",
                                        listing.price
                                    ),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Text(
                                    text = if (
                                        listing.category.equals(
                                            "Tutorías",
                                            ignoreCase = true
                                        )
                                    ) {
                                        " / hora"
                                    } else {
                                        ""
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = if (canEdit) {
                                onEditClick
                            } else {
                                onContactClick
                            },
                            modifier = Modifier.height(
                                Spacing.MinTouchTarget
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(
                                Spacing.CornerMedium
                            )
                        ) {
                            Text(
                                text = if (canEdit) {
                                    "Editar publicación"
                                } else {
                                    "Pedir información →"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
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
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "← Volver al catálogo",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Detalle del servicio",
                            modifier = Modifier.padding(
                                horizontal = Spacing.Small,
                                vertical = 4.dp
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (
                !state.isLoading &&
                state.errorMessage == null &&
                listing != null
            ) {
                managementContent()
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Huge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                Spacing.Medium
                            )
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Cargando publicación...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                state.errorMessage != null -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Large),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(
                            Spacing.CornerLarge
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.Large),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                Spacing.Medium
                            )
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 32.sp
                            )

                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )

                            Button(
                                onClick = onRetryClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(
                                    Spacing.CornerMedium
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                listing == null -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Large),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(
                            Spacing.CornerLarge
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.Huge),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                Spacing.Medium
                            )
                        ) {
                            Text(
                                text = "🔍",
                                fontSize = 36.sp
                            )

                            Text(
                                text = "Publicación no disponible",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "No se pudo encontrar la publicación solicitada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Spacer(
                                modifier = Modifier.height(
                                    Spacing.Small
                                )
                            )

                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(
                                    Spacing.CornerMedium
                                )
                            ) {
                                Text("Volver a explorar servicios")
                            }
                        }
                    }
                }

                else -> {
                    if (
                        listing.hasImage ||
                        listing.imageUrl.isNotBlank()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            listingImageContent(
                                listing,
                                Modifier.fillMaxSize()
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                    alpha = 0.85f
                                ),
                                shape = RoundedCornerShape(
                                    Spacing.CornerSmall
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(Spacing.Medium)
                            ) {
                                Text(
                                    text = "Categoría: ${listing.category}",
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(
                            Spacing.Large
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                Spacing.Small
                            )
                        ) {
                            Text(
                                text = listing.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 30.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(
                                    Spacing.Small
                                )
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(
                                        Spacing.CornerSmall
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.Small,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "★",
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 13.sp
                                        )

                                        Spacer(
                                            modifier = Modifier.width(4.dp)
                                        )

                                        Text(
                                            text = String.format(
                                                Locale.US,
                                                "%.1f",
                                                listing.rating
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )

                                        Text(
                                            text = " (${listing.reviewCount})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (
                                    listing.turnaroundTime.isNotBlank()
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(
                                            Spacing.CornerSmall
                                        )
                                    ) {
                                        Text(
                                            text = "⚡ ${listing.turnaroundTime}",
                                            modifier = Modifier.padding(
                                                horizontal = Spacing.Small,
                                                vertical = 4.dp
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(
                                Spacing.CornerLarge
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = listing.sellerName
                                            .firstOrNull()
                                            ?.uppercase()
                                            ?: "E",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                                        text = listing.sellerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = listing.faculty,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "Parte de la comunidad Nexora",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                Spacing.Small
                            )
                        ) {
                            Text(
                                text = "Descripción del servicio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = listing.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }

                        if (listing.includes.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(
                                    Spacing.CornerLarge
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        Spacing.Medium
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(
                                        10.dp
                                    )
                                ) {
                                    Text(
                                        text = "¿Qué incluye este servicio?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    listing.includes.forEach { includedItem ->
                                        Row(
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "✓",
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )

                                            Spacer(
                                                modifier = Modifier.width(
                                                    Spacing.Small
                                                )
                                            )

                                            Text(
                                                text = includedItem,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(
                                Spacing.CornerLarge
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍",
                                    fontSize = 22.sp
                                )

                                Spacer(
                                    modifier = Modifier.width(12.dp)
                                )

                                Column {
                                    Text(
                                        text = "Modalidad y lugar de atención",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    Text(
                                        text = "${listing.deliveryMode} • Consultá los detalles con quien publica.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        reviewContent()

                        Spacer(
                            modifier = Modifier.height(
                                Spacing.Large
                            )
                        )
                    }
                }
            }
        }
    }
}

