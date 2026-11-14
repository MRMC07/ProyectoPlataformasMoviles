package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.domain.model.Listing

@Composable
fun ListingStatusControls(
    listing: Listing,
    canManage: Boolean,
    isUpdating: Boolean,
    errorMessage: String?,
    onChangeStatus: (Boolean) -> Unit
) {
    if (!canManage && listing.isActive) return

    var pendingStatus by rememberSaveable(listing.id) {
        mutableStateOf<Boolean?>(null)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (listing.isActive) {
                    "Publicación activa"
                } else {
                    "Publicación retirada"
                },
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (listing.isActive) {
                    "Tu publicación aparece en el catálogo."
                } else if (canManage) {
                    "Tu publicación está fuera del catálogo. Podés editarla o reactivarla."
                } else {
                    "Esta publicación ya no está disponible para consultas."
                }
            )

            if (canManage) {
                OutlinedButton(
                    onClick = {
                        pendingStatus = !listing.isActive
                    },
                    enabled = !isUpdating
                ) {
                    Text(
                        text = if (listing.isActive) {
                            "Retirar publicación"
                        } else {
                            "Reactivar publicación"
                        }
                    )
                }

                if (isUpdating) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Actualizando publicación...")
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    val targetStatus = pendingStatus

    if (targetStatus != null && canManage) {
        AlertDialog(
            onDismissRequest = {
                pendingStatus = null
            },
            title = {
                Text(
                    text = if (targetStatus) {
                        "Reactivar publicación"
                    } else {
                        "Retirar publicación"
                    }
                )
            },
            text = {
                Text(
                    text = if (targetStatus) {
                        "La publicación volverá a aparecer en el catálogo."
                    } else {
                        "La publicación dejará de aparecer en el catálogo. Sus datos se conservarán y podrás reactivarla desde tu perfil."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onChangeStatus(targetStatus)
                        pendingStatus = null
                    },
                    enabled = !isUpdating
                ) {
                    Text(
                        text = if (targetStatus) {
                            "Reactivar"
                        } else {
                            "Retirar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingStatus = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}