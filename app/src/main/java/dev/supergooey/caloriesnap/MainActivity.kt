package dev.supergooey.caloriesnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
    enterTransition = { scaleIn(initialScale = 1.05f) + fadeIn() },
    exitTransition = { scaleOut(targetScale = 0.95f) + fadeOut() },
    popEnterTransition = { scaleIn(initialScale = 0.95f) + fadeIn() },
    popExitTransition = { scaleOut(targetScale = 1.05f) + fadeOut() }
  ) {
    composable("home") {
      HomeScreen {
        navController.navigate("camera")
      }
    }
    composable("camera") {
      CameraScreen()
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(goToCamera: () -> Unit = {}) {
  val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

  Scaffold(
    floatingActionButton = {
      LargeFloatingActionButton(
        onClick = {
          if (cameraPermissionState.status.isGranted) {
            goToCamera()
          } else {
            cameraPermissionState.launchPermissionRequest()
          }
        }
      ) {
        Icon(
          modifier = Modifier.size(32.dp),
          imageVector = Icons.Default.Person,
          contentDescription = "Take a picture"
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .consumeWindowInsets(paddingValues)
        .background(color = MaterialTheme.colorScheme.background),
      contentAlignment = Alignment.Center
    ) {
      Text(text = "Home")
    }
  }
}

@Composable
fun CameraScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(color = Color.Black), contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Camera",
      color = Color.White
    )
  }
}

@Preview
@Composable
fun AppPreview() {
  CalorieSnapTheme {
    App()
  }
}
