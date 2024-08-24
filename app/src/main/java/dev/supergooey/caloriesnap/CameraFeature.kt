package dev.supergooey.caloriesnap

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.ui.theme.CoolRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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

  val transition = updateTransition(
    targetState = state.step,
    label = ""
  )

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

  when (state.step) {
    CameraFeatureStep.Camera -> {
      Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
          modifier = Modifier.fillMaxSize(),
          factory = { ctx ->
            PreviewView(ctx).apply {
              layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
              setBackgroundColor(android.graphics.Color.BLACK)
              scaleType = PreviewView.ScaleType.FIT_CENTER
            }
          },
          update = { view ->
            view.controller = cameraController
            cameraController.bindToLifecycle(lifecycleOwner)
          }
        )

        Box(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .clickable { takePicture() }
              .background(color = CoolRed)
              .align(Alignment.BottomCenter),
          )
        }
      }
    }

    CameraFeatureStep.Analysis -> {

    }

    CameraFeatureStep.FreezeFrame -> {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(color = Color.Black),
        contentAlignment = Alignment.Center
      ) {
        if (state.capturedPhoto != null) {
          Image(
            bitmap = state.capturedPhoto.asImageBitmap(),
            contentDescription = "Photo"
          )
        }
      }
    }
  }
}

enum class CameraFeatureStep {
  Camera,
  FreezeFrame,
  Analysis
}

interface CameraFeature {
  data class State(
    val capturedPhoto: Bitmap? = null,
    val messages: List<MessageContent.Text>? = null,
    val step: CameraFeatureStep = CameraFeatureStep.Camera
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
          internalState.update {
            it.copy(
              capturedPhoto = action.bitmap,
              step = CameraFeatureStep.FreezeFrame
            )
          }
//          val request = MessagesRequest(
//            messages = listOf(
//              Message(
//                role = "user",
//                content = listOf(
//                  MessageContent.Image(
//                    source = ImageSource(
//                      data = action.bitmap.toBase64()
//                    )
//                  ),
//                  MessageContent.Text("What is in this image?")
//                )
//              )
//            )
//          )

          // Send it to Claude
//          val response = ImageToCalorieClient.api.getMessages(request)
//          Log.d("Camera", "Claude Response: $response")
//
//          if (response.isSuccessful) {
//            internalState.update { current ->
//              current.copy(
//                capturedPhoto = action.bitmap,
//                messages = response.body()!!.content.filterIsInstance<MessageContent.Text>()
//              )
//            }
//          } else {
//            Log.d("Camera", "Claude Error Response: ${response.errorBody()?.string()}")
//          }
        }
      }
    }
  }

  private fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val store: CameraStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CameraViewModel(store) as T
    }
  }
}

