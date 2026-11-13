package com.example.proyectoplataformasmoviles

import com.example.proyectoplataformasmoviles.ui.theme.ProyectoPlataformasMovilesTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.proyectoplataformasmoviles.domain.model.defaultUvgListings
import com.example.proyectoplataformasmoviles.presentation.home.HomeUiState
import com.example.proyectoplataformasmoviles.screens.HomeScreen
import com.example.proyectoplataformasmoviles.screens.ServiceOrderScreen
import org.junit.Rule
import org.junit.Test

class CatalogoInterfazTest {
    @get:Rule val interfaz = createComposeRule()

    @Test fun catalogoVacioNoMuestraOfertasDeEjemplo() {
        interfaz.setContent { ProyectoPlataformasMovilesTheme {
            HomeScreen(HomeUiState(isLoading = false), {}, {}, {})
        } }
        interfaz.onNodeWithText(defaultUvgListings.first().title).assertDoesNotExist()
        interfaz.onNodeWithText("Servicios Disponibles (0)").assertExists()
    }

    @Test fun solicitudInexistenteNoCambiaDeVendedor() {
        interfaz.setContent { ProyectoPlataformasMovilesTheme {
            ServiceOrderScreen(listingId = "inexistente", onBackClick = {}, onProceedToChat = { _, _, _ -> }, listings = emptyList())
        } }
        interfaz.onNodeWithText(defaultUvgListings.first().title).assertDoesNotExist()
        interfaz.onNodeWithText("Revisar consulta →").assertDoesNotExist()
    }
    @Test fun ofertaNuevaConservaSuPrecioVisible() {
        val oferta = defaultUvgListings.first().copy(
            id = "prueba_nexora", title = "Tutoría de cálculo", sellerName = "Estudiante de prueba", price = 75.50, imageUrl = ""
        )
        interfaz.setContent { ProyectoPlataformasMovilesTheme {
            HomeScreen(HomeUiState(listings = listOf(
                oferta,
                oferta.copy(id = "diseno_prueba", title = "Diseño de presentaciones", category = "Diseño", sellerName = "Estudio de prueba", price = 95.0),
                oferta.copy(id = "comida_prueba", title = "Brownies de chocolate", category = "Comida", sellerName = "Cocina de prueba", price = 20.0),
                oferta.copy(id = "tecnologia_prueba", title = "Soporte para tu laptop", category = "Tecnología", sellerName = "Servicio de prueba", price = 60.0)
            ), isLoading = false), {}, {}, {})
        } }
        interfaz.onNodeWithText("Q75.50").assertIsDisplayed()
        interfaz.waitForIdle()
        capturarInterfaz("inicio-datos-de-prueba")
    }}


