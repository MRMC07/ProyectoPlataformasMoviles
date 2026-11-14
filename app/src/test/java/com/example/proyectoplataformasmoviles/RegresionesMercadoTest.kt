package com.example.proyectoplataformasmoviles

import com.example.proyectoplataformasmoviles.domain.model.*
import com.example.proyectoplataformasmoviles.data.repository.leerPublicacion
import org.junit.Assert.*
import org.junit.Test

class RegresionesMercadoTest {
    @Test fun `conserva los centavos visibles`() { assertEquals("75.50", formatearImporte(75.50)) }
    @Test fun `rechaza precios ambiguos y acepta coma decimal`() {
        assertEquals(7550L, importeEnCentavos("75,50"))
        listOf("0", "-1", "NaN", "1.999", "1000000", "1e3").forEach { assertNull(importeEnCentavos(it)) }
    }
    @Test fun `no sustituye una publicacion inexistente`() {
        assertNull(buscarPublicacion(defaultUvgListings, "publicacion_inexistente"))
    }
    @Test fun `conserva la publicacion seleccionada`() {
        val real = defaultUvgListings.first().copy(id = "nueva", sellerName = "Vendedora real", price = 75.50)
        assertEquals(real, buscarPublicacion(listOf(real), "nueva"))
    }
    @Test fun `lee productos existentes sin inventar reputacion`() {
        val real = leerPublicacion("1", mapOf("nombre" to "Guitarra", "precio" to 250L))!!
        assertEquals(250.0, real.price, 0.0)
        assertEquals(0, real.reviewCount)
        assertEquals(0.0, real.rating, 0.0)
        assertTrue(real.includes.isEmpty())
    }
    @Test fun `omite datos invalidos`() {
        assertNull(leerPublicacion("x", mapOf("nombre" to "Algo", "precio" to Double.NaN)))
        assertNull(leerPublicacion("x", mapOf("nombre" to "Algo", "precio" to -1)))
        assertNull(leerPublicacion("x", mapOf("precio" to 10)))
    }
}
