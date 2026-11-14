package com.example.proyectoplataformasmoviles

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class RecorridoAplicacionTest {
    @get:Rule val interfaz = createAndroidComposeRule<MainActivity>()

    @Test fun registroRequiereCredencialesAntesDeEnviar() {
        interfaz.waitForIdle()
        interfaz.onNodeWithText("Crear mi cuenta").assertIsDisplayed().performClick()
        interfaz.onNodeWithText("Correo electrónico").assertIsDisplayed()
        interfaz.onNodeWithText("Crear mi cuenta").assertIsNotEnabled()
        interfaz.waitForIdle()
    }

    @Test fun conservaAccesoALosBorradoresLocalesDesdePerfil() {
        interfaz.onNodeWithText("Explorar Nexora").assertIsDisplayed().performClick()
        interfaz.onNodeWithText("Perfil").assertIsDisplayed().performClick()
        interfaz.onNodeWithText("Borradores de este dispositivo").assertIsDisplayed().performClick()
        interfaz.onNodeWithText("Mis borradores").assertIsDisplayed()
    }
}


