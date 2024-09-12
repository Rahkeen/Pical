package dev.supergooey.caloriesnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.supergooey.caloriesnap.features.dailylog.DailyLogScreen
import dev.supergooey.caloriesnap.features.dailylog.DailyLogViewModel
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CalorieSnapTheme {
        App()
//        CameraScreenAnalyzePreview()
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
  ) {
    composable("home") {
      val model = viewModel<DailyLogViewModel>(
        factory = DailyLogViewModel.Factory(
          logStore = MealLogDatabase.getDatabase(context)
        )
      )
      val state by model.state.collectAsState()

      DailyLogScreen(state = state, action = model::actions) { location ->
        navController.navigate(location.route)
      }
    }
    composable(
      route = "camera",
      enterTransition = { slideInHorizontally { it } },
      exitTransition = { slideOutHorizontally { it } }
    ) {
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
    composable(
      route = "log/{logId}",
      arguments = listOf(
        navArgument("logId") { type = NavType.IntType }
      )
    ) { backStackEntry ->
      val id = backStackEntry.arguments?.getInt("logId")!!
      val model = viewModel<FoodEntryViewModel>(
        factory = FoodEntryViewModel.Factory(
          logId = id,
          logDatabase = MealLogDatabase.getDatabase(context)
        )
      )
      val state by model.state.collectAsState()
      FoodEntryScreen(
        state = state,
        actions = model::actions,
        navigate = { location ->
          when (location) {
            FoodEntryFeature.Location.Back -> {
              navController.popBackStack()
            }
          }
        }
      )
    }
    composable("history") {
      val model = viewModel<HistoryViewModel>(
        factory = HistoryViewModel.Factory(
          logDao = MealLogDatabase.getDatabase(context).mealLogDao()
        )
      )
      val state by model.state.collectAsState()
      HistoryScreen(state)
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
