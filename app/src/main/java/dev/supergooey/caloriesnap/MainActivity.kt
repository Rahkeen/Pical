package dev.supergooey.caloriesnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalorieSnapTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = "home",
        enterTransition = { scaleIn(initialScale = 1.10f) + fadeIn() },
        exitTransition = { scaleOut(targetScale = 0.95f) + fadeOut() },
        popEnterTransition = { scaleIn(initialScale = 0.95f) + fadeIn() },
        popExitTransition = { scaleOut(targetScale = 1.10f) + fadeOut() }
    ) {
        composable("home") {
            val model = viewModel<HomeViewModel>(
                factory = HomeViewModel.Factory(
                    logStore = MealLogDatabase.getDatabase(context)
                )
            )
            val state by model.state.collectAsState()

            HomeScreen(state) {
                navController.navigate("camera")
            }
        }
        composable("camera") {
            val cameraViewModel = viewModel<CameraViewModel>(
                factory = CameraViewModel.Factory(
                    store = context.cameraStore(),
                    db = MealLogDatabase.getDatabase(context)
                )
            )
            val state by cameraViewModel.state.collectAsState()
            CameraScreen(
                state = state,
                actions = {
                    cameraViewModel.actions(it, navController)
                }
            )
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    CalorieSnapTheme {
        App()
    }
}
