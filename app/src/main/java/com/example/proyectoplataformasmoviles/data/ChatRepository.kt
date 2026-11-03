package com.example.proyectoplataformasmoviles.data

import com.example.proyectoplataformasmoviles.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("messages")


    fun getMessagesForChat(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = messagesCollection
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.timestamp } ?: emptyList() // Ordenamiento local seguro

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val messageMap = hashMapOf(
            "chatId" to chatId,
            "senderId" to senderId,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        messagesCollection.add(messageMap).await()
    }
}