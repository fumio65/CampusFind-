package com.campusfind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.campusfind.ui.theme.CampusFindTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * FILE: app/src/main/java/com/campusfind/MainActivity.kt
 *
 * Single Activity for CampusFind+ (DEC-013).
 * @AndroidEntryPoint required so Hilt can inject into this Activity
 * and all hiltViewModel() calls inside Composables work correctly.
 *
 * TODO TASK-123: Replace Surface body with CampusFindNavGraph(rememberNavController())
 *
 * See: DEC-013, DEC-022, TASK-123
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusFindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO TASK-123: CampusFindNavGraph(rememberNavController())
                }
            }
        }
    }
}