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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dev.supergooey.caloriesnap.ui.theme.CoolGreen
import dev.supergooey.caloriesnap.ui.theme.CoolRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

fun Bitmap.rotate(degrees: Int): Bitmap {
  val matrix = Matrix().apply {
    postRotate(degrees.toFloat())
  }

  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CameraScreen(
  state: CameraFeature.State,
  actions: (CameraFeature.Action) -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val cameraController = remember { LifecycleCameraController(context) }
  val transition = updateTransition(targetState = state.step, label = "Camera Step")
  val animationDuration = remember { 800 }
  val easing = remember { EaseInOutQuint }
  val cornerRadius by transition.animateDp(
    transitionSpec = {
      tween(durationMillis = animationDuration, easing = easing)
    },
    label = "Image Radius"
  ) { step ->
    when (step) {
      CameraFeatureStep.Camera -> 0.dp
      CameraFeatureStep.Analysis -> 12.dp
    }
  }

  fun takePicture() {
    val executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(
      executor,
      object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
          val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
          actions(CameraFeature.Action.TakePhoto(bitmap))
        }
      }
    )
  }

  SharedTransitionLayout {
    transition.AnimatedContent(
      transitionSpec = { fadeIn().togetherWith(fadeOut()) },
    ) { step ->
      when (step) {
        CameraFeatureStep.Camera -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(color = Color.Black),
            contentAlignment = Alignment.Center
          ) {
            if (state.capturedPhoto == null) {
              AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                  PreviewView(ctx).apply {
                    layoutParams =
                      ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
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
            } else {
              Image(
                modifier = Modifier
                  .sharedElement(
                    state = rememberSharedContentState(key = "image"),
                    boundsTransform = { _, _ ->
                      tween(
                        durationMillis = animationDuration,
                        easing = easing
                      )
                    },
                    animatedVisibilityScope = this@AnimatedContent
                  )
                  .clip(RoundedCornerShape(cornerRadius)),
                bitmap = state.capturedPhoto.asImageBitmap(),
                contentDescription = "Photo"
              )
            }
          }
        }

        CameraFeatureStep.Analysis -> {
          val rotation = remember { Animatable(0f) }
          val listState = rememberLazyListState()

          LaunchedEffect(state.messages) {
            if (listState.layoutInfo.totalItemsCount > 0) {
              listState.animateScrollToItem(listState.layoutInfo.totalItemsCount-1)
            }
          }

          LaunchedEffect(Unit) {
            delay(animationDuration.toLong())
            actions(CameraFeature.Action.AnalyzePhoto(state.capturedPhoto!!))
          }

          LaunchedEffect(state.loading) {
            if (state.loading) {
              delay(animationDuration.toLong())
              while (true) {
                rotation.animateTo(
                  2f,
                  animationSpec = tween(durationMillis = 200, easing = EaseInOut)
                )
                rotation.animateTo(
                  -2f,
                  animationSpec = tween(durationMillis = 200, easing = EaseInOut)
                )
              }
            } else {
              rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            }
          }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(color = Color.Black)
              .windowInsetsPadding(WindowInsets.statusBars)
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Image(
              modifier = Modifier
                .sharedElement(
                  state = rememberSharedContentState("image"),
                  animatedVisibilityScope = this@AnimatedContent,
                  boundsTransform = { _, _ ->
                    tween(durationMillis = animationDuration, easing = easing)
                  },
                )
                .graphicsLayer {
                  rotationZ = rotation.value
                }
                .size(300.dp)
                .clip(RoundedCornerShape(cornerRadius)),
              bitmap = state.capturedPhoto!!.asImageBitmap(),
              contentScale = ContentScale.Crop,
              contentDescription = "Photo"
            )
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              state = listState,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(items = state.messages) { message ->
                if (message.content[0] is MessageContent.Text) {
                  val alignment = if (message.role == "user") Alignment.TopEnd else Alignment.TopStart
                  val color = if (message.role == "user") MaterialTheme.colorScheme.primaryContainer else Color.White
                  val textColor = if (message.role == "user") MaterialTheme.colorScheme.onPrimaryContainer else Color.Black

                  val content = message.content[0] as MessageContent.Text
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .wrapContentHeight(),
                    contentAlignment = alignment
                  ) {
                    Text(
                      modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = color)
                        .padding(8.dp),
                      text = content.text,
                      color = textColor
                    )
                  }
                }
              }
            }
            AnimatedVisibility(state.mealResponse?.valid == true) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Add some context",
                  style = TextStyle(
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                )
                BasicTextField(
                  value = state.contextMessage,
                  onValueChange = { actions(CameraFeature.Action.UpdateContextMessage(it)) },
                  textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Go
                  ),
                  keyboardActions = KeyboardActions(onGo = { actions(CameraFeature.Action.SendContextMessage) }),
                  cursorBrush = SolidColor(value = MaterialTheme.colorScheme.onPrimaryContainer)
                ) { innerTextField ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .wrapContentHeight()
                      .clip(
                        RoundedCornerShape(8.dp)
                      )
                      .background(color = MaterialTheme.colorScheme.primaryContainer)
                      .padding(vertical = 16.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Box(modifier = Modifier.weight(1f)) {
                      innerTextField()
                    }
                    Icon(
                      modifier = Modifier
                        .size(24.dp)
                        .clickable { actions(CameraFeature.Action.SendContextMessage) },
                      imageVector = Icons.AutoMirrored.Rounded.Send,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      contentDescription = "Send Context Message"
                    )
                  }
                }
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically
            ) {
              AnimatedVisibility(state.mealResponse?.valid == true) {
                Box(
                  modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
                    .padding(8.dp)
                ) {
                  Text(
                    text = "Calories: ${state.mealResponse?.totalCalories}",
                    color = Color.Black
                  )
                }
              }
              AnimatedVisibility(state.mealResponse?.valid == true) {
                Button(
                  onClick = { actions(CameraFeature.Action.LogMeal) },
                  colors = ButtonDefaults.buttonColors(containerColor = CoolGreen)
                ) {
                  Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Save"
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

enum class CameraFeatureStep {
  Camera,
  Analysis
}


interface CameraFeature {
  data class State(
    val capturedPhoto: Bitmap? = null,
    val mealResponse: MealResponse? = null,
    val messages: List<Message> = emptyList(),
    val contextMessage: String = "",
    val step: CameraFeatureStep = CameraFeatureStep.Camera,
    val loading: Boolean = false
  )

  sealed interface Action {
    data class TakePhoto(val bitmap: Bitmap) : Action
    data class AnalyzePhoto(val bitmap: Bitmap) : Action
    data object LogMeal : Action
    data class UpdateContextMessage(val message: String): Action
    data object SendContextMessage: Action
  }
}

class CameraViewModel(
  private val store: CameraStore,
  private val db: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(CameraFeature.State())
  val state = internalState.asStateFlow()

  fun actions(action: CameraFeature.Action, navController: NavHostController) {
    when (action) {
      is CameraFeature.Action.TakePhoto -> {
        Log.d("Camera", "Saved Photo: ${action.bitmap}")
        viewModelScope.launch {
          internalState.update {
            it.copy(
              capturedPhoto = action.bitmap,
              step = CameraFeatureStep.Analysis
            )
          }
        }
      }

      is CameraFeature.Action.AnalyzePhoto -> {
        internalState.update { it.copy(loading = true) }
        val messages = mutableListOf<Message>()
        val initialMessage =
          Message(
            role = "user",
            content = listOf(
              MessageContent.Image(
                source = ImageSource(
                  data = action.bitmap.toBase64()
                )
              )
            )
          )
        messages.add(initialMessage)
        viewModelScope.launch {
          // Send it to Claude
          val response = ImageToCalorieClient.api.getMessages(MessagesRequest(messages = messages))
          Log.d("Camera", "Claude Response: $response")

          if (response.isSuccessful) {
            val messagesResponse = response.body()!!
            val meal = Json.decodeFromString<MealResponse>(
              messagesResponse
                .content
                .filterIsInstance<MessageContent.Text>()
                .first()
                .text
            )
            val mealMessage = Message(
              role = messagesResponse.role,
              content = listOf(
                MessageContent.Text(text = meal.foodDescription)
              )
            )
            messages.add(mealMessage)
            internalState.update { current ->
              current.copy(
                loading = false,
                mealResponse = meal,
                messages = messages.toList()
              )
            }
          } else {
            Log.d("Camera", "Claude Error Response: ${response.errorBody()?.string()}")
            internalState.update { current -> current.copy( loading = false, ) }
          }
        }
      }

      CameraFeature.Action.LogMeal -> {
        viewModelScope.launch {
          val uri = store.saveImageLocally(state.value.capturedPhoto!!).getOrNull()
          val log = state.value.mealResponse!!.toMealLog(uri)
          db.mealLogDao().insertMealLog(log)
          navController.navigate("home")
        }
      }

      CameraFeature.Action.SendContextMessage -> {
        viewModelScope.launch {
          val message = Message(
            role = "user",
            content = listOf(
              MessageContent.Text(text = internalState.value.contextMessage)
            )
          )
          val messages = internalState.value.messages.toMutableList().apply {
            add(message)
          }
          internalState.update { it.copy(loading = true, contextMessage = "", messages = messages.toList()) }

          val response = ImageToCalorieClient.api.getMessages(MessagesRequest(messages = messages))

          if (response.isSuccessful) {
            val messagesResponse = response.body()!!
            Log.d("Camera", "Claude Context Response: $messagesResponse")
            val meal = Json.decodeFromString<MealResponse>(
              messagesResponse
                .content
                .filterIsInstance<MessageContent.Text>()
                .first()
                .text
            )
            val mealMessage = Message(
              role = messagesResponse.role,
              content = listOf(
                MessageContent.Text(text = meal.foodDescription)
              )
            )
            messages.add(mealMessage)
            internalState.update { current ->
              current.copy(
                loading = false,
                mealResponse = meal,
                messages = messages.toList()
              )
            }
          } else {
            Log.d("Camera", "Claude Error Response: ${response.errorBody()?.string()}")
            internalState.update { it.copy(loading = false) }
          }
        }
      }

      is CameraFeature.Action.UpdateContextMessage -> {
        internalState.update { it.copy(contextMessage = action.message) }
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
  class Factory(private val store: CameraStore, private val db: MealLogDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CameraViewModel(store, db) as T
    }
  }
}

fun MessagesResponse.toMessage(): Message {
  return Message(
    role = role,
    content = content
  )
}

fun MealResponse.toMealLog(
  imageUri: String?,
  timestamp: Long = System.currentTimeMillis()
): MealLog {
  return MealLog(
    foodTitle = foodTitle,
    foodDescription = foodDescription,
    totalCalories = totalCalories,
    valid = valid,
    imageUri = imageUri,
    time = timestamp
  )
}

