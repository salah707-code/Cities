package com.example.areadirectory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.areadirectory.data.api.ApiClient
import com.example.areadirectory.data.loader.LocalDataLoader
import com.example.areadirectory.data.model.Category
import com.example.areadirectory.data.model.Country
import com.example.areadirectory.data.model.District
import com.example.areadirectory.data.model.Governorate
import com.example.areadirectory.data.model.Place
import com.example.areadirectory.data.repository.PlacesRepository
import com.example.areadirectory.data.repository.PlacesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface SearchUiState {
    data object Initial : SearchUiState
    data object Loading : SearchUiState
    data class Success(
        val results: List<Place>,
        val district: String,
        val category: String
    ) : SearchUiState
    data object Empty : SearchUiState
    sealed interface Error : SearchUiState {
        data object Network : Error
        data object Api : Error
    }
}

data class SearchFormState(
    val countries: List<Country> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCountry: Country? = null,
    val selectedGovernorate: Governorate? = null,
    val selectedDistrict: District? = null,
    val selectedCategory: Category? = null,
    val backendUrl: String = BuildConfig.DEFAULT_BACKEND_URL
) {
    val isSearchEnabled: Boolean
        get() = selectedCountry != null &&
                selectedGovernorate != null &&
                selectedDistrict != null &&
                selectedCategory != null

    val currentGovernorates: List<Governorate>
        get() = selectedCountry?.governorates.orEmpty()

    val currentDistricts: List<District>
        get() = selectedGovernorate?.districts.orEmpty()
}

class SearchViewModel(
    application: Application,
    private var repository: PlacesRepository? = null
) : AndroidViewModel(application) {

    private val _formState = MutableStateFlow(SearchFormState())
    val formState: StateFlow<SearchFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadData()
        if (repository == null) {
            val api = ApiClient.createPlacesApi(_formState.value.backendUrl)
            repository = PlacesRepositoryImpl(api)
        }
    }

    private fun loadData() {
        try {
            val (countries, categories) = LocalDataLoader.loadData(getApplication())
            _formState.update { current ->
                val defaultCountry = countries.firstOrNull()
                val defaultGov = defaultCountry?.governorates?.firstOrNull()
                current.copy(
                    countries = countries,
                    categories = categories,
                    selectedCountry = defaultCountry,
                    selectedGovernorate = defaultGov,
                    selectedDistrict = null,
                    selectedCategory = null
                )
            }
        } catch (_: Exception) {
            // Keep empty if load fails
        }
    }

    fun selectCountry(country: Country) {
        _formState.update { current ->
            val firstGov = country.governorates.firstOrNull()
            current.copy(
                selectedCountry = country,
                selectedGovernorate = firstGov,
                selectedDistrict = null
            )
        }
    }

    fun selectGovernorate(governorate: Governorate) {
        _formState.update { current ->
            current.copy(
                selectedGovernorate = governorate,
                selectedDistrict = null
            )
        }
    }

    fun selectDistrict(district: District) {
        _formState.update { current ->
            current.copy(selectedDistrict = district)
        }
    }

    fun selectCategory(category: Category) {
        _formState.update { current ->
            current.copy(selectedCategory = category)
        }
    }

    fun updateBackendUrl(newUrl: String) {
        val cleanUrl = newUrl.trim()
        if (cleanUrl.isEmpty()) return
        _formState.update { it.copy(backendUrl = cleanUrl) }
        val api = ApiClient.createPlacesApi(cleanUrl)
        repository = PlacesRepositoryImpl(api)
    }

    fun search() {
        val state = _formState.value
        if (!state.isSearchEnabled) return

        val country = state.selectedCountry?.nameAr ?: return
        val governorate = state.selectedGovernorate?.nameAr ?: return
        val district = state.selectedDistrict?.nameAr ?: return
        val category = state.selectedCategory?.id ?: return
        val displayCategory = state.selectedCategory.nameAr

        _uiState.value = SearchUiState.Loading

        viewModelScope.launch {
            try {
                val currentRepo = repository ?: run {
                    val api = ApiClient.createPlacesApi(state.backendUrl)
                    val newRepo = PlacesRepositoryImpl(api)
                    repository = newRepo
                    newRepo
                }

                val places = currentRepo.searchPlaces(
                    district = district,
                    governorate = governorate,
                    country = country,
                    category = category
                )

                if (places.isEmpty()) {
                    _uiState.value = SearchUiState.Empty
                } else {
                    _uiState.value = SearchUiState.Success(
                        results = places,
                        district = district,
                        category = displayCategory
                    )
                }
            } catch (_: IOException) {
                _uiState.value = SearchUiState.Error.Network
            } catch (_: HttpException) {
                _uiState.value = SearchUiState.Error.Api
            } catch (_: Exception) {
                _uiState.value = SearchUiState.Error.Api
            }
        }
    }

    fun retry() {
        search()
    }

    fun resetSearch() {
        _uiState.value = SearchUiState.Initial
    }
}
