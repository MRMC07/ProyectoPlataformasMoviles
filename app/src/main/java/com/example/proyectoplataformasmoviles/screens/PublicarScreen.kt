package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarScreen(
    onBackClick: () -> Unit,
    onPublicarClick: () -> Unit
) {

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaExpandida by remember { mutableStateOf(false) }

    val categorias = listOf(
        "Tutorías",
        "Comida",
        "Tecnología",
        "Diseño"
    )

    // Validaciones en tiempo real

    val errorTitulo = titulo.isBlank()
    val errorDescripcion = descripcion.isBlank()

    val precioValido = precio.toDoubleOrNull() != null &&
            (precio.toDoubleOrNull() ?: 0.0) > 0.0
    val errorPrecio = precio.isNotBlank() && !precioValido

    val errorCategoria = categoria.isBlank()

    val formularioValido =
        !errorTitulo &&
                !errorDescripcion &&
                precioValido &&
                !errorCategoria

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                onClick = onBackClick,
                modifier = Modifier.size(
                    width = 55.dp,
                    height = 42.dp
                ),
                shape = RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = "←",
                    fontSize = 20.sp
                )
            }

            Spacer(
                modifier = Modifier.size(10.dp)
            )

            Text(
                text = "Publicar",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // Título

        OutlinedTextField(
            value = titulo,
            onValueChange = {
                titulo = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Título del producto o servicio")
            },
            isError = errorTitulo,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorTitulo) {

            Text(
                text = "El título no puede estar vacío.",
                color = Color(0xFFB3261E),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 4.dp
                )
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // Descripción

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Descripción")
            },
            isError = errorDescripcion,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorDescripcion) {

            Text(
                text = "La descripción no puede estar vacía.",
                color = Color(0xFFB3261E),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 4.dp
                )
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // Precio

        OutlinedTextField(
            value = precio,
            onValueChange = {
                precio = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Precio (Q)")
            },
            isError = errorPrecio,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorPrecio) {

            Text(
                text = "Ingresa un precio válido mayor a 0.",
                color = Color(0xFFB3261E),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 4.dp
                )
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // Categoría

        ExposedDropdownMenuBox(
            expanded = categoriaExpandida,
            onExpandedChange = {
                categoriaExpandida = !categoriaExpandida
            }
        ) {

            OutlinedTextField(
                value = categoria,
                onValueChange = { },
                readOnly = true,
                label = {
                    Text("Categoría")
                },
                isError = errorCategoria,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = categoriaExpandida
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )

            DropdownMenu(
                expanded = categoriaExpandida,
                onDismissRequest = {
                    categoriaExpandida = false
                },
                modifier = Modifier.exposedDropdownSize()
            ) {

                categorias.forEach { opcion ->

                    DropdownMenuItem(
                        text = {
                            Text(opcion)
                        },
                        onClick = {
                            categoria = opcion
                            categoriaExpandida = false
                        }
                    )
                }
            }
        }

        if (errorCategoria) {

            Text(
                text = "Selecciona una categoría.",
                color = Color(0xFFB3261E),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 4.dp
                )
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onPublicarClick,
            enabled = formularioValido,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text = "Publicar",
                fontSize = 16.sp
            )
        }
    }
}