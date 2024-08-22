package dev.supergooey.caloriesnap

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.ui.theme.CoolRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun Bitmap.rotate(degrees: Int): Bitmap {
  val matrix = Matrix().apply {
    postRotate(degrees.toFloat())
  }

  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

@Composable
fun CameraScreen(
  state: CameraFeature.State,
  actions: (CameraFeature.Action) -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val cameraController = remember { LifecycleCameraController(context) }

  fun takePicture() {
    val executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(
      executor,
      object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
          val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
          actions(CameraFeature.Action.SavePhoto(bitmap))
        }
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { ctx ->
        PreviewView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
          setBackgroundColor(android.graphics.Color.BLACK)
          scaleType = PreviewView.ScaleType.FILL_CENTER
        }
      },
      update = { view ->
        view.controller = cameraController
        cameraController.bindToLifecycle(lifecycleOwner)
      }
    )

    Box(
      modifier = Modifier
        .windowInsetsPadding(WindowInsets.navigationBars)
        .size(60.dp)
        .clip(CircleShape)
        .clickable { takePicture() }
        .background(color = CoolRed)
        .align(Alignment.BottomCenter),
    )

    if (state.capturedPhoto != null) {
      Image(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .padding(start = 16.dp)
          .size(100.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(width = 2.dp, color = Color(1f, 1f, 1f), shape = RoundedCornerShape(16.dp))
          .align(Alignment.BottomStart),
        bitmap = state.capturedPhoto.asImageBitmap(),
        contentScale = ContentScale.Crop,
        contentDescription = ""
      )
    }
  }
}


interface CameraFeature {
  data class State(
    val capturedPhoto: Bitmap? = null
  )

  sealed class Action {
    data class SavePhoto(val bitmap: Bitmap) : Action()
  }
}

class CameraViewModel(
  private val store: CameraStore
) : ViewModel() {
  private val internalState = MutableStateFlow(CameraFeature.State())
  val state = internalState.asStateFlow()

  fun actions(action: CameraFeature.Action) {
    when (action) {
      is CameraFeature.Action.SavePhoto -> {
        Log.d("Camera", "Saved Photo: ${action.bitmap}")
        viewModelScope.launch {
          val result = store.saveImageLocally(action.bitmap)
          Log.d("Camera", "Saved Photo Locally: $result")
          internalState.update { current ->
            current.copy(capturedPhoto = action.bitmap)
          }
        }
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val store: CameraStore): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CameraViewModel(store) as T
    }
  }
}

