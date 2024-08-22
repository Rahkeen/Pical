package dev.supergooey.caloriesnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

  NavHost(
    navController = navController,
    startDestination = "home",
    enterTransition = { scaleIn(initialScale = 1.10f) + fadeIn() },
    exitTransition = { scaleOut(targetScale = 0.95f) + fadeOut() },
    popEnterTransition = { scaleIn(initialScale = 0.95f) + fadeIn() },
    popExitTransition = { scaleOut(targetScale = 1.10f) + fadeOut() }
  ) {
    composable("home") {
      HomeScreen {
        navController.navigate("camera")
      }
    }
    composable("camera") {
      val context = LocalContext.current.applicationContext
      val model = viewModel<CameraViewModel>(
        factory = CameraViewModel.Factory(
          store = CameraStore(context)
        )
      )
      val state by model.state.collectAsState()
      CameraScreen(
        state = state,
        actions = model::actions
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
