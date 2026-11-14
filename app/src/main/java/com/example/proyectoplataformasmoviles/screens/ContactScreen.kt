package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.proyectoplataformasmoviles.ui.theme.UvgSuccess
import com.example.proyectoplataformasmoviles.ui.theme.UvgSurface
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextMuted
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextPrimary
import com.example.proyectoplataformasmoviles.ui.theme.UvgTextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessageItem(
    val id: Long,
    val text: String,
    val timestamp: String,
    val isMine: Boolean,
    val isSystemNotification: Boolean = false,
    val isLocalDraft: Boolean = false
)

@Composable
fun ContactScreen(
    listingId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    modality: String = "Lugar a convenir",
    initialNote: String = "",
    schedule: String = "Martes o Jueves por la tarde",
    clientName: String = "Estudiante",
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
                            text = "No se puede iniciar el chat porque el servicio no está disponible.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UvgTextSecondary,
                            textAlign = TextAlign.Center
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

    var draftMessage by remember(listingId) { mutableStateOf("") }
    var isDealConfirmed by remember(listingId) { mutableStateOf(false) }
    var showReviewDialog by remember(listingId) { mutableStateOf(false) }

    // Hilo de mensajes en memoria (Borrador local explícito)
    val messages = remember(listingId) {
        mutableStateListOf(
            ChatMessageItem(
                id = 1L,
                text = "📌 Solicitud Iniciada: Has seleccionado \"${listing.title}\" por Q${String.format(Locale.US, "%.2f", listing.price)}. Modalidad: $modality. Horario propuesto: $schedule.",
                timestamp = "10:30 AM",
                isMine = false,
                isSystemNotification = true,
                isLocalDraft = true
            ),
            ChatMessageItem(
                id = 2L,
                text = if (initialNote.isNotBlank()) initialNote else "¡Hola ${listing.sellerName}! Me interesa contratar tu servicio.",
                timestamp = "10:31 AM",
                isMine = true,
                isLocalDraft = true
            ),
            ChatMessageItem(
                id = 3L,
                text = "¡Hola! Gracias por escribirme. Me parece perfecto el horario que sugieres para \"${listing.title}\". ¿Nos encontramos en el campus o prefieres coordinar por llamada?",
                timestamp = "10:33 AM",
                isMine = false,
                isLocalDraft = true
            )
        )
    }

    val quickChips = listOf(
        "📍 Acordemos un lugar para encontrarnos",
        "💻 Prefiero enlace de Google Meet",
        "💳 ¿Aceptas transferencia bancaria?",
        "✅ Confirmo la fecha acordada"
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage(textToSend: String) {
        if (textToSend.isBlank()) return
        val newId = (messages.maxOfOrNull { it.id } ?: 0L) + 1L
        messages.add(
            ChatMessageItem(
                id = newId,
                text = textToSend.trim(),
                timestamp = "Ahora",
                isMine = true,
                isLocalDraft = true
            )
        )
        draftMessage = ""
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun confirmDeal() {
        if (isDealConfirmed) return
        isDealConfirmed = true
        val newId = (messages.maxOfOrNull { it.id } ?: 0L) + 1L
        messages.add(
            ChatMessageItem(
                id = newId,
                text = "📌 Intención de acuerdo registrada (Borrador local en memoria): Se ha marcado la solicitud de \"${listing.title}\" con ${listing.sellerName} por Q${String.format(Locale.US, "%.2f", listing.price)}. Recuerda acordar el medio de entrega y pago formalmente.",
                timestamp = "Ahora",
                isMine = false,
                isSystemNotification = true,
                isLocalDraft = true
            )
        )
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = UvgBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Header del Emprendedor (Pizarrón: "Chat E./C. X")
            Surface(
                color = UvgSurface,
                border = BorderStroke(1.dp, UvgBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Medium, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBackClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = UvgGreenPrimary)
                    ) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(Spacing.ExtraSmall))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(UvgGreenLight)
                            .border(1.dp, UvgGreenBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = listing.sellerName.firstOrNull()?.uppercase() ?: "E",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = UvgGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.Medium))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = listing.sellerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = UvgTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓", color = UvgSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            text = "En línea • ${listing.faculty}",
                            fontSize = 11.sp,
                            color = UvgGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Badge de Estado del Trato
                    Surface(
                        color = if (isDealConfirmed) UvgGreenLight else UvgGoldLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isDealConfirmed) UvgGreenBorder else UvgGoldDark.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = if (isDealConfirmed) "✅ Acuerdo local" else "🟡 En negociación",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDealConfirmed) UvgGreenDark else UvgGoldDark,
                            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Tarjeta Fija del Contexto del Servicio
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                color = UvgSurface,
                shape = RoundedCornerShape(Spacing.CornerMedium),
                border = BorderStroke(1.dp, UvgBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(Spacing.CornerSmall))
                            .background(UvgGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤝", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(Spacing.Small))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = UvgTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Q${String.format(Locale.US, "%.2f", listing.price)} • $modality",
                            fontSize = 11.sp,
                            color = UvgGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!isDealConfirmed) {
                        Button(
                            onClick = { confirmDeal() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UvgGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(Spacing.CornerSmall),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Confirmar trato", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { showReviewDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UvgGoldDark,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(Spacing.CornerSmall),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("⭐ Calificar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Aviso sobre estado en memoria (Prioridad 4 de Codex)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium, vertical = 2.dp),
                color = UvgGoldLight.copy(alpha = 0.5f),
                shape = RoundedCornerShape(Spacing.CornerSmall),
                border = BorderStroke(1.dp, UvgGoldDark.copy(alpha = 0.15f))
            ) {
                Text(
                    text = "ℹ️ Vista preliminar: Mensajes y acuerdos actuales se gestionan en memoria local hasta la sincronización definitiva con el servidor.",
                    fontSize = 10.sp,
                    color = UvgGoldDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    lineHeight = 14.sp
                )
            }

            // 3. Hilo de Conversación
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
                }

                items(messages, key = { it.id }) { message ->
                    if (message.isSystemNotification) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = if (message.text.contains("acuerdo")) UvgGreenLight else UvgGoldLight,
                            shape = RoundedCornerShape(Spacing.CornerMedium),
                            border = BorderStroke(
                                1.dp,
                                if (message.text.contains("acuerdo")) UvgGreenBorder else UvgGoldDark.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = if (message.text.contains("acuerdo")) UvgGreenDark else UvgGoldDark,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        ChatBubbleRow(message = message)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.Small))
                }
            }

            // 4. Chips de Respuestas Rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.Medium, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickChips.forEach { chipText ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Spacing.CornerLarge))
                            .clickable {
                                if (chipText.contains("Confirmo")) {
                                    confirmDeal()
                                } else {
                                    sendMessage(chipText)
                                }
                            },
                        color = UvgSurface,
                        shape = RoundedCornerShape(Spacing.CornerLarge),
                        border = BorderStroke(1.dp, UvgBorder)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            color = UvgTextPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 5. Compositor de Mensajes
            Surface(
                color = UvgSurface,
                border = BorderStroke(1.dp, UvgBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Medium, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draftMessage,
                        onValueChange = { draftMessage = it },
                        placeholder = { Text("Escribe un mensaje al emprendedor...", fontSize = 13.sp, color = UvgTextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(Spacing.CornerPill),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UvgGreenPrimary,
                            unfocusedBorderColor = UvgBorder,
                            focusedContainerColor = UvgBackground,
                            unfocusedContainerColor = UvgBackground
                        )
                    )

                    Spacer(modifier = Modifier.width(Spacing.Small))

                    Button(
                        onClick = { sendMessage(draftMessage) },
                        enabled = draftMessage.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UvgGreenPrimary,
                            disabledContainerColor = UvgGreenPrimary.copy(alpha = 0.3f),
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("➤", fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            if (showReviewDialog) {
                ReviewDialog(
                    serviceTitle = listing.title,
                    sellerName = listing.sellerName,
                    onDismissRequest = { showReviewDialog = false },
                    onSubmitReview = { rating, comment ->
                        showReviewDialog = false
                        val newId = (messages.maxOfOrNull { it.id } ?: 0L) + 1L
                        messages.add(
                            ChatMessageItem(
                                id = newId,
                                text = "⭐ Reseña registrada (Demostración local): Valoraste a ${listing.sellerName} con $rating estrellas. \"${if (comment.isNotBlank()) comment else "Excelente servicio"}\".",
                                timestamp = "Ahora",
                                isMine = true,
                                isSystemNotification = true,
                                isLocalDraft = true
                            )
                        )
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleRow(message: ChatMessageItem) {
    val isMine = message.isMine

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Surface(
                color = if (isMine) UvgGreenLight else UvgSurface,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isMine) 14.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 14.dp
                ),
                border = BorderStroke(
                    1.dp,
                    if (isMine) UvgGreenBorder else UvgBorder
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = if (isMine) UvgGreenDark else UvgTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = UvgTextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (message.isLocalDraft) {
                    Text(
                        text = "• Local",
                        fontSize = 9.sp,
                        color = UvgTextMuted
                    )
                }
            }
        }
    }
}
