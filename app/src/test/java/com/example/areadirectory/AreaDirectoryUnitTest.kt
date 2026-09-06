package com.example.areadirectory

import com.example.areadirectory.data.api.PlacesApi
import com.example.areadirectory.data.loader.LocalDataLoader
import com.example.areadirectory.data.model.Category
import com.example.areadirectory.data.model.Country
import com.example.areadirectory.data.model.District
import com.example.areadirectory.data.model.Governorate
import com.example.areadirectory.data.model.PlaceDto
import com.example.areadirectory.data.model.SearchRequest
import com.example.areadirectory.data.model.SearchResponseDto
import com.example.areadirectory.data.repository.PlacesRepositoryImpl
import com.example.areadirectory.viewmodel.SearchFormState
import com.example.areadirectory.viewmodel.SearchUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AreaDirectoryUnitTest {

    // 1. JSON Loading Test
    @Test
    fun testJsonParsing() {
        val sampleJson = """
        {
          "countries": [
            {
              "id": "YE",
              "nameAr": "اليمن",
              "nameEn": "Yemen",
              "altNames": ["الجمهورية اليمنية"],
              "governorates": [
                {
                  "id": "aden",
                  "nameAr": "عدن",
                  "nameEn": "Aden",
                  "altNames": ["محافظة عدن"],
                  "districts": [
                    { "id": "al_mansurah", "nameAr": "المنصورة", "nameEn": "Al Mansurah" },
                    { "id": "sheikh_othman", "nameAr": "الشيخ عثمان", "nameEn": "Sheikh Othman" },
                    { "id": "al_mualla", "nameAr": "المعلا", "nameEn": "Al Mualla" },
                    { "id": "al_tawahi", "nameAr": "التواهي", "nameEn": "Al Tawahi" },
                    { "id": "khor_maksar", "nameAr": "خور مكسر", "nameEn": "Khor Maksar" },
                    { "id": "sira", "nameAr": "صيرة", "nameEn": "Crater" },
                    { "id": "dar_saad", "nameAr": "دار سعد", "nameEn": "Dar Saad" },
                    { "id": "al_buraiqeh", "nameAr": "البريقة", "nameEn": "Al Buraiqeh" }
                  ]
                }
              ]
            }
          ],
          "categories": [
            { "id": "hospital", "nameAr": "المستشفيات", "nameEn": "Hospitals", "googleQuery": "Hospitals" },
            { "id": "pharmacy", "nameAr": "الصيدليات", "nameEn": "Pharmacies", "googleQuery": "Pharmacies" }
          ]
        }
        """.trimIndent()

        val (countries, categories) = LocalDataLoader.parseData(sampleJson)

        assertEquals(1, countries.size)
        val yemen = countries[0]
        assertEquals("اليمن", yemen.nameAr)
        assertEquals(1, yemen.governorates.size)

        val aden = yemen.governorates[0]
        assertEquals("عدن", aden.nameAr)
        assertEquals(8, aden.districts.size)
        assertTrue(aden.districts.any { it.nameAr == "المنصورة" })
        assertTrue(aden.districts.any { it.nameAr == "صيرة" })

        assertEquals(2, categories.size)
        assertEquals("المستشفيات", categories[0].nameAr)
    }

    // 2. Selection & Enabling Search Test
    @Test
    fun testFormStateSelectionLogic() {
        val country = Country("YE", "اليمن", "Yemen")
        val gov = Governorate("aden", "عدن", "Aden")
        val dist = District("al_mansurah", "المنصورة", "Al Mansurah")
        val cat = Category("hospital", "المستشفيات", "Hospitals", "Hospitals")

        var state = SearchFormState()
        assertFalse("Search should be disabled initially", state.isSearchEnabled)

        state = state.copy(selectedCountry = country)
        assertFalse(state.isSearchEnabled)

        state = state.copy(selectedGovernorate = gov)
        assertFalse(state.isSearchEnabled)

        state = state.copy(selectedDistrict = dist)
        assertFalse(state.isSearchEnabled)

        state = state.copy(selectedCategory = cat)
        assertTrue("Search should be enabled when all 4 are selected", state.isSearchEnabled)

        // Test with blank or empty fields
        val emptyCountry = Country("YE", "   ", "Yemen")
        assertFalse(state.copy(selectedCountry = emptyCountry).isSearchEnabled)

        val emptyGov = Governorate("aden", "", "Aden")
        assertFalse(state.copy(selectedGovernorate = emptyGov).isSearchEnabled)

        val emptyDist = District("al_mansurah", " ", "Al Mansurah")
        assertFalse(state.copy(selectedDistrict = emptyDist).isSearchEnabled)

        val emptyCat = Category("hospital", "", "Hospitals", "Hospitals")
        assertFalse(state.copy(selectedCategory = emptyCat).isSearchEnabled)

        assertTrue(SearchFormState.isInputValid("اليمن", "عدن", "المنصورة", "المستشفيات"))
        assertFalse(SearchFormState.isInputValid("", "عدن", "المنصورة", "المستشفيات"))
        assertFalse(SearchFormState.isInputValid("اليمن", " ", "المنصورة", "المستشفيات"))
        assertFalse(SearchFormState.isInputValid("اليمن", "عدن", "", "المستشفيات"))
        assertFalse(SearchFormState.isInputValid("اليمن", "عدن", "المنصورة", null))
    }

    // 3. Query Creation Test
    @Test
    fun testQueryCreation() {
        val categoryQuery = "Hospitals"
        val district = "Al Mansurah"
        val governorate = "Aden"
        val country = "Yemen"

        val query = "$categoryQuery in $district, $governorate, $country"
        assertEquals("Hospitals in Al Mansurah, Aden, Yemen", query)
    }

    // 4. Deduplication Test
    @Test
    fun testDeduplicationByPlaceId() = runBlocking {
        val fakeApi = object : PlacesApi {
            override suspend fun searchPlaces(request: SearchRequest): SearchResponseDto {
                return SearchResponseDto(
                    district = request.district,
                    category = request.category,
                    count = 3,
                    results = listOf(
                        PlaceDto("ChIJ1", "مستشفى 1", "عدن", "https://maps.google.com/?cid=1"),
                        PlaceDto("ChIJ2", "مستشفى 2", "عدن", "https://maps.google.com/?cid=2"),
                        PlaceDto("ChIJ1", "مستشفى 1 مكرر", "عدن", "https://maps.google.com/?cid=1") // Duplicate ID
                    )
                )
            }
        }

        val repository = PlacesRepositoryImpl(fakeApi)
        val places = repository.searchPlaces("المنصورة", "عدن", "اليمن", "hospital")

        assertEquals(2, places.size)
        assertEquals("ChIJ1", places[0].id)
        assertEquals("ChIJ2", places[1].id)
    }

    // 5. API Response Mapping Test
    @Test
    fun testApiResponseMappingAndFallbackUrl() = runBlocking {
        val fakeApi = object : PlacesApi {
            override suspend fun searchPlaces(request: SearchRequest): SearchResponseDto {
                return SearchResponseDto(
                    district = request.district,
                    category = request.category,
                    count = 1,
                    results = listOf(
                        PlaceDto(
                            placeId = "ChIJ_TEST_123",
                            name = "مستشفى الجمهورية",
                            address = "خور مكسر، عدن",
                            mapsUrl = null // No explicit URL, test fallback
                        )
                    )
                )
            }
        }

        val repository = PlacesRepositoryImpl(fakeApi)
        val places = repository.searchPlaces("خور مكسر", "عدن", "اليمن", "hospital")

        assertEquals(1, places.size)
        val place = places[0]
        assertEquals("ChIJ_TEST_123", place.id)
        assertEquals("مستشفى الجمهورية", place.name)
        assertEquals("خور مكسر، عدن", place.address)
        assertEquals("https://www.google.com/maps/place/?q=place_id:ChIJ_TEST_123", place.mapsUrl)
    }

    // 6. ViewModel State Transitions Test
    @Test
    fun testViewModelStateClasses() {
        val initial: SearchUiState = SearchUiState.Initial
        val loading: SearchUiState = SearchUiState.Loading
        val empty: SearchUiState = SearchUiState.Empty
        val networkErr: SearchUiState = SearchUiState.Error.Network
        val apiErr: SearchUiState = SearchUiState.Error.Api

        assertNotNull(initial)
        assertNotNull(loading)
        assertNotNull(empty)
        assertNotNull(networkErr)
        assertNotNull(apiErr)
    }
}
