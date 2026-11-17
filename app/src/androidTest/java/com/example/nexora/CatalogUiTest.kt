package com.example.nexora

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nexora.domain.model.defaultUvgListings
import com.example.nexora.presentation.home.HomeUiState
import com.example.nexora.screens.HomeScreen
import com.example.nexora.screens.ServiceOrderScreen
import com.example.nexora.ui.theme.NexoraTheme
import org.junit.Rule
import org.junit.Test

class CatalogUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyCatalogDoesNotShowSampleListings() {
        composeRule.setContent {
            NexoraTheme {
                HomeScreen(
                    state = HomeUiState(isLoading = false),
                    onListingClick = {},
                    onPublishClick = {},
                    onRetryClick = {}
                )
            }
        }

        composeRule
            .onNodeWithText(defaultUvgListings.first().title)
            .assertDoesNotExist()

        composeRule
            .onNodeWithText("Servicios disponibles (0)")
            .assertExists()
    }

    @Test
    fun missingListingDoesNotDisplayAnotherSeller() {
        composeRule.setContent {
            NexoraTheme {
                ServiceOrderScreen(
                    listingId = "missing_listing",
                    onBackClick = {},
                    onProceedToChat = { _, _, _ -> },
                    listings = emptyList()
                )
            }
        }

        composeRule
            .onNodeWithText(defaultUvgListings.first().title)
            .assertDoesNotExist()

        composeRule
            .onNodeWithText("Revisar consulta →")
            .assertDoesNotExist()
    }

    @Test
    fun newListingPreservesItsDisplayedPrice() {
        val tutoringListing = defaultUvgListings.first().copy(
            id = "tutoring_test",
            title = "Tutoría de cálculo",
            sellerName = "Estudiante de prueba",
            price = 75.50,
            imageUrl = ""
        )

        val listings = listOf(
            tutoringListing,
            tutoringListing.copy(
                id = "design_test",
                title = "Diseño de presentaciones",
                category = "Diseño",
                sellerName = "Estudio de prueba",
                price = 95.0
            ),
            tutoringListing.copy(
                id = "food_test",
                title = "Brownies de chocolate",
                category = "Comida",
                sellerName = "Cocina de prueba",
                price = 20.0
            ),
            tutoringListing.copy(
                id = "technology_test",
                title = "Soporte para tu laptop",
                category = "Tecnología",
                sellerName = "Servicio de prueba",
                price = 60.0
            )
        )

        composeRule.setContent {
            NexoraTheme {
                HomeScreen(
                    state = HomeUiState(
                        listings = listings,
                        isLoading = false
                    ),
                    onListingClick = {},
                    onPublishClick = {},
                    onRetryClick = {}
                )
            }
        }

        composeRule
            .onNodeWithText("Q75.50")
            .assertIsDisplayed()

        composeRule.waitForIdle()
        captureUiScreenshot("home-test-data")
    }
}