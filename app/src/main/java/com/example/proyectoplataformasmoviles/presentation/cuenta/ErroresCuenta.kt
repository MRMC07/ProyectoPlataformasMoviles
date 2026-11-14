package com.example.proyectoplataformasmoviles.presentation.cuenta

fun codigoErrorCuenta(codigoFirebase: String?, detalle: String?): String {
    val detalleSeguro = detalle.orEmpty()
    return when {
        detalleSeguro.contains("CONFIGURATION_NOT_FOUND") -> "CONFIGURATION_NOT_FOUND"
        detalleSeguro.contains("API key not valid", ignoreCase = true) -> "ERROR_INVALID_API_KEY"
        codigoFirebase?.matches(Regex("ERROR_[A-Z_]+")) == true -> codigoFirebase
        else -> "ERROR_DESCONOCIDO"
    }
}

fun mensajeErrorCuenta(codigo: String): String = when (codigo) {
    "ERROR_OPERATION_NOT_ALLOWED", "ERROR_PASSWORD_LOGIN_DISABLED", "CONFIGURATION_NOT_FOUND" ->
        "El acceso por correo todavía no está habilitado en Nexora. El equipo debe completar la configuración de Authentication en Firebase."
    "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya tiene una cuenta. Probá la pestaña Ingresar."
    "ERROR_WEAK_PASSWORD" -> "La contraseña no cumple la política de seguridad. Elegí una más segura."
    "ERROR_INVALID_EMAIL" -> "Revisá el formato de tu correo electrónico."
    "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" ->
        "El correo o la contraseña no coinciden. Revisalos y volvé a intentar."
    "ERROR_NETWORK_REQUEST_FAILED" -> "No pudimos conectar con Firebase. Revisá la conexión a internet y volvé a intentar."
    "ERROR_TOO_MANY_REQUESTS" -> "Hubo demasiados intentos. Esperá unos minutos antes de volver a intentar."
    "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada. Contactá al equipo de Nexora."
    "ERROR_INVALID_API_KEY", "ERROR_APP_NOT_AUTHORIZED", "ERROR_API_NOT_AVAILABLE" ->
        "La configuración de Nexora no está autorizada para acceder a Firebase. El equipo debe revisarla."
    else -> "No pudimos completar el acceso. Volvé a intentar. Referencia: $codigo."
}
