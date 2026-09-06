package com.example.areadirectory

import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.areadirectory.data.loader.LocalDataLoader
import com.example.areadirectory.ui.HomeScreen
import com.example.areadirectory.ui.theme.AreaDirectoryTheme
import com.example.areadirectory.viewmodel.SearchFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AreaDirectoryRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadDataFromAssets() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (countries, categories) = LocalDataLoader.loadData(context)

        assertTrue(countries.isNotEmpty())
        val yemen = countries.find { it.id == "YE" }
        assertTrue("Yemen country must exist", yemen != null)

        val aden = yemen?.governorates?.find { it.id == "aden" }
        assertTrue("Aden governorate must exist", aden != null)
        assertEquals(8, aden?.districts?.size)

        // Verify required districts
        val districtNames = aden?.districts?.map { it.nameAr } ?: emptyList()
        assertTrue(districtNames.contains("المنصورة"))
        assertTrue(districtNames.contains("الشيخ عثمان"))
        assertTrue(districtNames.contains("المعلا"))
        assertTrue(districtNames.contains("التواهي"))
        assertTrue(districtNames.contains("خور مكسر"))
        assertTrue(districtNames.contains("صيرة"))
        assertTrue(districtNames.contains("دار سعد"))
        assertTrue(districtNames.contains("البريقة"))

        // Verify categories
        assertTrue(categories.isNotEmpty())
        val catNames = categories.map { it.nameAr }
        assertTrue(catNames.contains("المستشفيات"))
        assertTrue(catNames.contains("الصيدليات"))
        assertTrue(catNames.contains("المدارس"))
        assertTrue(catNames.contains("المساجد"))
    }

    @Test
    fun testHomeScreenUiComponents() {
        val formState = SearchFormState()

        composeTestRule.setContent {
            AreaDirectoryTheme {
                HomeScreen(
                    formState = formState,
                    onCountrySelected = {},
                    onGovernorateSelected = {},
                    onDistrictSelected = {},
                    onCategorySelected = {},
                    onSearchClick = {},
                    onUpdateBackendUrl = {}
                )
            }
        }

        // Header exists
        composeTestRule.onNodeWithTag("header_card").assertExists()
        // Form card exists
        composeTestRule.onNodeWithTag("selection_form_card").assertExists()
        // Search button exists and is disabled
        composeTestRule.onNodeWithTag("search_button").assertExists().assertIsNotEnabled()
    }
}
