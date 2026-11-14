package com.example.proyectoplataformasmoviles.domain.model

enum class RequestStatus(val label: String) {
    PENDING("Pendiente"),
    ACCEPTED("Aceptada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada"),
    COMPLETED("Completada")
}

data class ServiceRequest(
    val id: String,
    val listingId: String,
    val listingTitle: String,
    val customerId: String,
    val customerName: String,
    val providerId: String,
    val providerName: String,
    val modality: String,
    val schedule: String,
    val note: String,
    val status: RequestStatus,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun availableStatuses(userId: String): List<RequestStatus> {
        return when {
            userId == customerId &&
                    status in listOf(
                RequestStatus.PENDING,
                RequestStatus.ACCEPTED
            ) -> listOf(RequestStatus.CANCELLED)

            userId == providerId &&
                    status == RequestStatus.PENDING -> listOf(
                RequestStatus.ACCEPTED,
                RequestStatus.REJECTED
            )

            userId == providerId &&
                    status == RequestStatus.ACCEPTED ->
                listOf(RequestStatus.COMPLETED)

            else -> emptyList()
        }
    }
}