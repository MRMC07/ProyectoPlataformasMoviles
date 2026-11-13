package com.example.proyectoplataformasmoviles.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.formatearImporte

@Composable
fun ContactoConectado(publicacion: Listing?, alVolver: () -> Unit,
    modalidad: String = "", horario: String = "", nota: String = "") {
    if (publicacion == null) { EstadoPublicacion(alVolver = alVolver); return }
    val contexto = LocalContext.current
    val mensaje = buildString {
        appendLine("Consulta sobre: ${publicacion.title}")
        appendLine("Precio publicado: Q${formatearImporte(publicacion.price)}")
        if (modalidad.isNotBlank()) appendLine("Modalidad propuesta: $modalidad")
        if (horario.isNotBlank()) appendLine("Horario propuesto: $horario")
        if (nota.isNotBlank()) { appendLine(); append(nota) }
    }
    Scaffold { margenes ->
        Column(Modifier.fillMaxSize().padding(margenes).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(publicacion.title, style = MaterialTheme.typography.headlineSmall)
            Text("Q${formatearImporte(publicacion.price)} · ${publicacion.sellerName}")
            Text("Revisá tu consulta. Podés enviarla desde tu aplicación de correo; todavía no se ha enviado ni confirmado un trato.")
            if (modalidad.isNotBlank()) Text("Modalidad propuesta: $modalidad")
            if (horario.isNotBlank()) Text("Horario propuesto: $horario")
            if (nota.isNotBlank()) Text(nota)
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(publicacion.correoVendedor).matches()) {
                Text(publicacion.correoVendedor)
                Button(onClick = {
                    val direccion = Uri.parse("mailto:${Uri.encode(publicacion.correoVendedor)}?subject=${Uri.encode("Consulta: ${publicacion.title}")}&body=${Uri.encode(mensaje)}")
                    try { contexto.startActivity(Intent(Intent.ACTION_SENDTO, direccion)) }
                    catch (_: ActivityNotFoundException) {
                        Toast.makeText(contexto, "No hay una aplicación de correo instalada. Usá el correo mostrado.", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Contactar por correo") }
            } else Text("Esta publicación no tiene un correo de contacto registrado.")
            TextButton(onClick = alVolver) { Text("Regresar") }
        }
    }
}
