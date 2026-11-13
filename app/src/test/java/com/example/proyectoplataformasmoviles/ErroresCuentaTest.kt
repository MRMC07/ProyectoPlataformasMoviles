package com.example.proyectoplataformasmoviles

import com.example.proyectoplataformasmoviles.presentation.cuenta.codigoErrorCuenta
import com.example.proyectoplataformasmoviles.presentation.cuenta.mensajeErrorCuenta
import org.junit.Assert.*
import org.junit.Test

class ErroresCuentaTest {
    @Test fun configuracionAusenteNoCulpaALasCredenciales() {
        val codigo = codigoErrorCuenta("ERROR_INTERNAL_ERROR", "An internal error has occurred. [ CONFIGURATION_NOT_FOUND ]")
        assertEquals("CONFIGURATION_NOT_FOUND", codigo)
        assertTrue(mensajeErrorCuenta(codigo).contains("configuración de Authentication"))
    }
    @Test fun cuentaExistenteIndicaIngresar() {
        assertTrue(mensajeErrorCuenta("ERROR_EMAIL_ALREADY_IN_USE").contains("pestaña Ingresar"))
    }
    @Test fun errorDeRedNoDiceQueLaContrasenaEsIncorrecta() {
        assertTrue(mensajeErrorCuenta("ERROR_NETWORK_REQUEST_FAILED").contains("conexión a internet"))
        assertFalse(mensajeErrorCuenta("ERROR_NETWORK_REQUEST_FAILED").contains("contraseña"))
    }
    @Test fun noMuestraDetallesSensiblesEnErroresDesconocidos() {
        val codigo = codigoErrorCuenta(null, "detalle interno con información de la cuenta")
        assertEquals("ERROR_DESCONOCIDO", codigo)
        assertFalse(mensajeErrorCuenta(codigo).contains("detalle interno"))
    }
}
