package com.example.areadirectory.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.areadirectory.ui.HomeScreen
import com.example.areadirectory.ui.ResultsScreen
import com.example.areadirectory.viewmodel.SearchViewModel

enum class Screen {
    HOME,
    RESULTS
}

@Composable
fun AppNavigation(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = currentScreen == Screen.RESULTS) {
        viewModel.resetSearch()
        currentScreen = Screen.HOME
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition",
        modifier = modifier
    ) { screen ->
        when (screen) {
            Screen.HOME -> {
                HomeScreen(
                    formState = formState,
                    onCountrySelected = { viewModel.selectCountry(it) },
                    onGovernorateSelected = { viewModel.selectGovernorate(it) },
                    onDistrictSelected = { viewModel.selectDistrict(it) },
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onSearchClick = {
                        viewModel.search()
                        currentScreen = Screen.RESULTS
                    },
                    onUpdateBackendUrl = { viewModel.updateBackendUrl(it) }
                )
            }
            Screen.RESULTS -> {
                ResultsScreen(
                    uiState = uiState,
                    onBackClick = {
                        viewModel.resetSearch()
                        currentScreen = Screen.HOME
                    },
                    onRetryClick = { viewModel.retry() }
                )
            }
        }
    }
}
