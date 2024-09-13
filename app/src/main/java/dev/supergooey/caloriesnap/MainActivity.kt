package dev.supergooey.caloriesnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import dev.supergooey.caloriesnap.features.capture.CaptureFeature
import dev.supergooey.caloriesnap.features.capture.CaptureScreen
import dev.supergooey.caloriesnap.features.capture.CaptureViewModel
import dev.supergooey.caloriesnap.features.dailylog.DailyLogScreen
import dev.supergooey.caloriesnap.features.dailylog.DailyLogViewModel
import dev.supergooey.caloriesnap.features.edit.EditLogFeature
import dev.supergooey.caloriesnap.features.edit.EditLogScreen
import dev.supergooey.caloriesnap.features.edit.EditLogViewModel
import dev.supergooey.caloriesnap.features.history.LogHistoryFeature
import dev.supergooey.caloriesnap.features.history.LogHistoryScreen
import dev.supergooey.caloriesnap.features.history.LogHistoryViewModel
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import dev.supergooey.caloriesnap.ui.theme.DURATION_EXTRA_LONG
import dev.supergooey.caloriesnap.ui.theme.EmphasizedEasing
import java.time.LocalDate

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
  SharedTransitionLayout {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    NavHost(
      modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background),
      navController = navController,
      enterTransition = {
        fadeIn(animationSpec = tween(durationMillis = DURATION_EXTRA_LONG, easing = EmphasizedEasing))
      },
      exitTransition = {
        fadeOut(animationSpec = tween(durationMillis = DURATION_EXTRA_LONG, easing = EmphasizedEasing))
      },
      startDestination = "logs",
    ) {
      composable(
        route = "logs?date={date}",
        arguments = listOf(
          navArgument("date") {
            type = NavType.StringType
            nullable = true
          }
        )
      ) { backStackEntry ->
        val dateString = backStackEntry.arguments?.getString("date")
        val date = if (dateString.isNullOrEmpty()) {
          LocalDate.now()
        } else {
          LocalDate.parse(dateString)
        }
        val model = viewModel<DailyLogViewModel>(
          factory = DailyLogViewModel.Factory(
            date = date,
            logStore = MealLogDatabase.getDatabase(context)
          )
        )
        val state by model.state.collectAsState()

        DailyLogScreen(
          state = state,
          sharedTransitionScope = this@SharedTransitionLayout,
          animatedVisibilityScope = this@composable,
          action = model::actions
        ) { location ->
          navController.navigate(location.route)
        }
      }
      composable(
        route = "capture",
        enterTransition = {
          slideInHorizontally(
            animationSpec = tween(
              durationMillis = DURATION_EXTRA_LONG,
              easing = EmphasizedEasing
            )
          ) { it }
        },
        exitTransition = {
          slideOutHorizontally(
            animationSpec = tween(
              durationMillis = DURATION_EXTRA_LONG,
              easing = EmphasizedEasing
            )
          ) { it }
        }
      ) {
        val model = viewModel<CaptureViewModel>(
          factory = CaptureViewModel.Factory(
            store = context.cameraStore(),
            db = MealLogDatabase.getDatabase(context)
          )
        )
        val state by model.state.collectAsState()
        CaptureScreen(
          state = state,
          actions = model::actions,
          navigation = { location ->
            when (location) {
              CaptureFeature.Location.Back -> {
                navController.popBackStack()
              }
            }
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
        val model = viewModel<EditLogViewModel>(
          factory = EditLogViewModel.Factory(
            logId = id,
            logDatabase = MealLogDatabase.getDatabase(context)
          )
        )
        val state by model.state.collectAsState()
        EditLogScreen(
          state = state,
          actions = model::actions,
          sharedTransitionScope = this@SharedTransitionLayout,
          animatedVisibilityScope = this@composable,
          navigate = { location ->
            when (location) {
              EditLogFeature.Location.Back -> {
                navController.popBackStack()
              }
            }
          }
        )
      }
      composable("history") {
        val model = viewModel<LogHistoryViewModel>(
          factory = LogHistoryViewModel.Factory(
            logDao = MealLogDatabase.getDatabase(context).mealLogDao()
          )
        )
        val state by model.state.collectAsState()
        LogHistoryScreen(
          state = state,
          sharedTransitionScope = this@SharedTransitionLayout,
          animatedVisibilityScope = this@composable
        ) { location ->
          when (location) {
            LogHistoryFeature.Location.Back -> {
              navController.popBackStack()
            }

            is LogHistoryFeature.Location.Logs -> {
              navController.navigate(location.route)
            }
          }
        }
      }
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
