
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

data class ChatMessage(
    val id: Long,
    val text: String,
    val timestamp: String,
    val isMine: Boolean
)

@Composable
fun ContactScreen(
    listingId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draftMessage by remember(listingId) {
        mutableStateOf("")
    }

    val messages = remember(listingId) {
        mutableStateListOf(
            ChatMessage(
                id = 1L,
                text = "¡Hola! ¿En qué puedo ayudarte? " +
                    "La tutoría puede ser virtual o presencial.",
                timestamp = "10:42 AM",
                isMine = false
            ),
            ChatMessage(
                id = 2L,
                text = "Hola, me interesa la tutoría. " +
                    "¿En qué zona te encuentras para ver si podemos " +
                    "hacerla presencial?",
                timestamp = "10:45 AM",
                isMine = true
            ),
            ChatMessage(
                id = 3L,
                text = "Estoy por la zona centro. También podemos hacer " +
                    "la tutoría en línea si te queda mejor.",
                timestamp = "10:48 AM",
                isMine = false
            )
        )
    }

    var nextMessageId by remember(listingId) {
        mutableStateOf(4L)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 10.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 26.sp,
                modifier = Modifier
                    .clickable(onClick = onBackClick)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCE8F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👩‍🏫",
                    fontSize = 21.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "María",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "✓",
                        color = Color(0xFF16854A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "En línea",
                    fontSize = 11.sp,
                    color = Color(0xFF16854A)
                )
            }

            Text(
                text = "☎",
                fontSize = 22.sp,
                color = Color(0xFF17345F)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3EAF2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📚",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tutorías de Matemática",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Q75 / hora",
                    fontSize = 12.sp,
                    color = Color(0xFF16854A),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "›",
                fontSize = 25.sp,
                color = Color(0xFF17345F)
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 10.dp
                )
                .background(
                    color = Color(0xFFA5F0C7),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "♢",
                fontSize = 20.sp,
                color = Color(0xFF126B43)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Recuerda: acuerda los detalles de la tutoría " +
                    "antes de realizar cualquier pago.",
                fontSize = 11.sp,
                color = Color(0xFF126B43)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(
                items = messages,
                key = { message -> message.id }
            ) { message ->
                ChatMessageItem(
                    message = message
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▧",
                fontSize = 22.sp,
                color = Color(0xFF17345F)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = draftMessage,
                onValueChange = { value ->
                    draftMessage = value
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Escribe un mensaje...",
                        fontSize = 12.sp
                    )
                },
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            FloatingActionButton(
                onClick = {
                    if (draftMessage.isNotBlank()) {
                        messages.add(
                            ChatMessage(
                                id = nextMessageId,
                                text = draftMessage.trim(),
                                timestamp = "Ahora",
                                isMine = true
                            )
                        )

                        nextMessageId++
                        draftMessage = ""
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = Color(0xFF17345F),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(
                    text = "➤",
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (message.isMine) {
                            Color(0xFF17345F)
                        } else {
                            Color(0xFFDDE6F0)
                        },
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 9.dp
                    )
            ) {
                Text(
                    text = message.text,
                    color = if (message.isMine) {
                        Color.White
                    } else {
                        Color(0xFF263238)
                    },
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = message.timestamp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 5.dp,
                    vertical = 2.dp
                ),
            fontSize = 9.sp,
            color = Color.Gray,
            textAlign = if (message.isMine) {
                TextAlign.End
            } else {
                TextAlign.Start
            }
        )
    }
}
