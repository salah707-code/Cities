package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.areadirectory.ui.navigation.AppNavigation
import com.example.areadirectory.ui.theme.AreaDirectoryTheme
import com.example.areadirectory.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AreaDirectoryTheme {
                AppNavigation(
                    viewModel = searchViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
