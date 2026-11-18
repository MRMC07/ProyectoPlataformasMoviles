package com.example.nexora.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nexora.domain.model.Listing
import com.example.nexora.domain.model.formatPrice

@Composable
fun ContactSummaryScreen(
    listing: Listing?,
    onBack: () -> Unit,
    modality: String = "",
    schedule: String = "",
    note: String = "",
    modifier: Modifier = Modifier
) {
    if (listing == null) {
        ListingUnavailableScreen(onBack = onBack)
        return
    }

    val context = LocalContext.current

    val messageBody = buildString {
        appendLine("Consulta sobre: ${listing.title}")
        appendLine("Precio publicado: Q${formatPrice(listing.price)}")

        if (modality.isNotBlank()) {
            appendLine("Modalidad propuesta: $modality")
        }

        if (schedule.isNotBlank()) {
            appendLine("Horario propuesto: $schedule")
        }

        if (note.isNotBlank()) {
            appendLine()
            append(note)
        }
    }

    Scaffold(
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = listing.title,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Q${formatPrice(listing.price)} · ${listing.sellerName}"
            )

            Text(
                text = "Revisá tu consulta. Podés enviarla desde tu aplicación de correo; todavía no se ha enviado ni confirmado un trato."
            )

            if (modality.isNotBlank()) {
                Text("Modalidad propuesta: $modality")
            }

            if (schedule.isNotBlank()) {
                Text("Horario propuesto: $schedule")
            }

            if (note.isNotBlank()) {
                Text(note)
            }

            if (
                Patterns.EMAIL_ADDRESS
                    .matcher(listing.sellerEmail)
                    .matches()
            ) {
                Text(listing.sellerEmail)

                Button(
                    onClick = {
                        val emailAddress = Uri.encode(listing.sellerEmail)
                        val subject = Uri.encode("Consulta: ${listing.title}")
                        val body = Uri.encode(messageBody)
                        val emailUri = Uri.parse(
                            "mailto:$emailAddress?subject=$subject&body=$body"
                        )

                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO, emailUri)
                            )
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                "No hay una aplicación de correo instalada. Usá el correo mostrado.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Text("Contactar por correo")
                }
            } else {
                Text(
                    text = "Esta publicación no tiene un correo de contacto registrado."
                )
            }

            TextButton(onClick = onBack) {
                Text("Regresar")
            }
        }
    }
}