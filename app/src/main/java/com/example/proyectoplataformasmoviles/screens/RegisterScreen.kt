package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.ui.theme.Espaciado
import com.example.proyectoplataformasmoviles.R
import com.example.proyectoplataformasmoviles.presentation.cuenta.ModeloCuenta

@Composable
fun RegisterScreen(onRegisterSuccess: (userName: String, role: String) -> Unit,
    onBackClick: () -> Unit, modifier: Modifier = Modifier,
    modeloCuenta: ModeloCuenta = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var modo by rememberSaveable { mutableIntStateOf(0) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val valido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() &&
        contrasena.length >= 6 && (modo == 1 || nombre.trim().length >= 3)
    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { margenes ->
        Column(Modifier.fillMaxSize().padding(margenes).imePadding().verticalScroll(rememberScrollState()).padding(Espaciado.amplio),
            verticalArrangement = Arrangement.spacedBy(Espaciado.amplio)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Espaciado.pequeno)) {
                IconButton(onClick = onBackClick, enabled = !modeloCuenta.ocupado) { Icon(painterResource(R.drawable.ic_regresar), "Regresar") }
                MarcaNexora()
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.pequeno)) {
                Text(if (modo == 0) "Tu talento merece\nsu propio espacio." else "Qué bueno\nverte de nuevo.",
                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Una comunidad para descubrir, publicar y conectar.", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TabRow(selectedTabIndex = modo, containerColor = MaterialTheme.colorScheme.background) {
                Tab(selected = modo == 0, onClick = { modo = 0 }, enabled = !modeloCuenta.ocupado, text = { Text("Crear cuenta") })
                Tab(selected = modo == 1, onClick = { modo = 1 }, enabled = !modeloCuenta.ocupado, text = { Text("Ingresar") })
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.normal)) {
                if (modo == 0) OutlinedTextField(nombre, { nombre = it.take(80) }, label = { Text("Tu nombre") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !modeloCuenta.ocupado, shape = RoundedCornerShape(14.dp))
                OutlinedTextField(correo, { correo = it }, label = { Text("Correo electrónico") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(),
                    enabled = !modeloCuenta.ocupado, shape = RoundedCornerShape(14.dp))
                OutlinedTextField(contrasena, { contrasena = it }, label = { Text("Contraseña") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(),
                    enabled = !modeloCuenta.ocupado, shape = RoundedCornerShape(14.dp),
                    supportingText = { if (modo == 0) Text("Usá al menos 6 caracteres.") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") } })
                modeloCuenta.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (modeloCuenta.ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
                Button(onClick = {
                    modeloCuenta.acceder(correo, contrasena, nombre, modo == 0) { usuario -> onRegisterSuccess(usuario, "Estudiante") }
                }, enabled = valido && !modeloCuenta.ocupado, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(if (modo == 0) "Crear mi cuenta" else "Ingresar a Nexora")
                }
            }
            Text("Para estudiantes de todas las universidades de Guatemala.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

