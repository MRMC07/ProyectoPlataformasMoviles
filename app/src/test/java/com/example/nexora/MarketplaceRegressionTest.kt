package com.example.nexora

import com.example.nexora.data.repository.parseListing
import com.example.nexora.domain.model.amountInCents
import com.example.nexora.domain.model.defaultUvgListings
import com.example.nexora.domain.model.findListing
import com.example.nexora.domain.model.formatPrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceRegressionTest {

    @Test
    fun formattedPricePreservesCents() {
        assertEquals("75.50", formatPrice(75.50))
    }

    @Test
    fun priceParserAcceptsDecimalCommaAndRejectsInvalidAmounts() {
        assertEquals(7550L, amountInCents("75,50"))

        val invalidAmounts = listOf(
            "0",
            "-1",
            "NaN",
            "1.999",
            "1000000",
            "1e3"
        )

        invalidAmounts.forEach { amount ->
            assertNull(amountInCents(amount))
        }
    }

    @Test
    fun missingListingDoesNotReturnAnotherListing() {
        assertNull(
            findListing(
                listings = defaultUvgListings,
                listingId = "missing_listing"
            )
        )
    }

    @Test
    fun selectedListingIsPreserved() {
        val selectedListing = defaultUvgListings.first().copy(
            id = "new_listing",
            sellerName = "Vendedora real",
            price = 75.50
        )

        assertEquals(
            selectedListing,
            findListing(
                listings = listOf(selectedListing),
                listingId = "new_listing"
            )
        )
    }

    @Test
    fun listingMapperDoesNotInventReputation() {
        val listing = requireNotNull(
            parseListing(
                listingId = "1",
                fields = mapOf(
                    "title" to "Guitarra",
                    "price" to 250L
                )
            )
        )

        assertEquals(250.0, listing.price, 0.0)
        assertEquals(0, listing.reviewCount)
        assertEquals(0.0, listing.rating, 0.0)
        assertTrue(listing.includes.isEmpty())
    }

    @Test
    fun listingMapperRejectsInvalidFields() {
        assertNull(
            parseListing(
                listingId = "invalid_price",
                fields = mapOf(
                    "title" to "Artículo",
                    "price" to Double.NaN
                )
            )
        )

        assertNull(
            parseListing(
                listingId = "negative_price",
                fields = mapOf(
                    "title" to "Artículo",
                    "price" to -1
                )
            )
        )

        assertNull(
            parseListing(
                listingId = "missing_title",
                fields = mapOf(
                    "price" to 10
                )
            )
        )
    }
}