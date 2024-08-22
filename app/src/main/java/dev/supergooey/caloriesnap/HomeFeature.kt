package dev.supergooey.caloriesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

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
    }
  }
}

class HomeViewModel: ViewModel() {

}