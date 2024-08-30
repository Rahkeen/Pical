package dev.supergooey.caloriesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
  state: HomeFeature.State,
  navigate: (HomeFeature.Location) -> Unit
) {
  val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

  Scaffold(
    floatingActionButton = {
      LargeFloatingActionButton(
        onClick = {
          if (cameraPermissionState.status.isGranted) {
            navigate(HomeFeature.Location.Camera)
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        .padding(horizontal = 8.dp),
      contentPadding = paddingValues,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
      items(items = state.logs) { log ->
        Row(
          modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.background)
            .clickable { navigate(HomeFeature.Location.Log(log.id)) },
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (log.imageUri != null) {
            AsyncImage(
              model = log.imageUri,
              modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop,
              contentDescription = log.foodDescription
            )
          } else {
            Box(
              modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surface),
              contentAlignment = Alignment.Center
            ) {
              Text("🥒")
            }
          }
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = log.foodTitle ?: "Food Title")
            Text(text = "Calories: ${log.totalCalories}")
          }
        }
      }
      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

interface HomeFeature {
  data class State(
    val logs: List<MealLog>
  )

  sealed class Location(val route: String) {
    data object Camera : Location("camera")
    data class Log(val id: Int) : Location("log/$id")
  }
}

class HomeViewModel(
  logStore: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(HomeFeature.State(logs = emptyList()))
  private val logsFlow = logStore.mealLogDao().getMealLogsByTime()
  val state = combine(internalState.asStateFlow(), logsFlow) { state, logs ->
    state.copy(logs = logs)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = HomeFeature.State(emptyList())
  )

  @Suppress("UNCHECKED_CAST")
  class Factory(private val logStore: MealLogDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HomeViewModel(logStore) as T
    }
  }
}

