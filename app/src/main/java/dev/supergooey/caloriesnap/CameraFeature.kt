package dev.supergooey.caloriesnap

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
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
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import dev.supergooey.caloriesnap.ui.theme.CoolGreen
import dev.supergooey.caloriesnap.ui.theme.CoolOrange
import dev.supergooey.caloriesnap.ui.theme.CoolRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.time.LocalDate

@Preview
@Composable
fun CameraScreenAnalyzePreview() {
  CalorieSnapTheme(darkTheme = true) {
    val bitmap = ImageBitmap.imageResource(R.drawable.bibimbap).asAndroidBitmap()
    var state by remember {
      mutableStateOf(
        CameraFeature.State(
          capturedPhoto = bitmap,
          mealResponse = MealResponse(
            foodTitle = "Bibimbap",
            foodDescription = "This is some bibibibibibibibibimbap bap bap bap hahahah hahhaha klajsdhfkajsdh",
            totalCalories = 1000,
            valid = true
          ),
          messages = listOf(
            Message(
              role = "system",
              content = listOf(
                MessageContent.Text(
                  text = "This is some bibibibibibibibibimbap bap bap bap hahahah hahhaha klajsdhfkajsdh"
                )
              )
            ),
            Message(
              role = "user",
              content = listOf(
                MessageContent.Text(
                  text = "I think you might be a bit broken there buddy."
                )
              )
            ),
          ),
          step = CameraFeatureStep.Analysis
        )
      )
    }
    CameraScreen(
      state = state,
      actions = { action ->
        when (action) {
          is CameraFeature.Action.AnalyzePhoto -> {}
          CameraFeature.Action.SendContextMessage -> {
            state = state.copy(
              contextMessage = "",
              messages = state.messages + Message(
                role = "user",
                content = listOf(MessageContent.Text(state.contextMessage))
              )
            )
          }

          is CameraFeature.Action.UpdateContextMessage -> {
            state = state.copy(
              contextMessage = action.message
            )
          }

          CameraFeature.Action.LogMeal -> {}
          is CameraFeature.Action.TakePhoto -> {}
        }
      }
    )
  }
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

  CalorieSnapTheme(darkTheme = true) {
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
                  val shape1 = remember {
                    RoundedPolygon.circle(
                      numVertices = 12
                    )
                  }
                  val shape2 = remember {
                    RoundedPolygon.star(
                      numVerticesPerRadius = 12,
                      innerRadius = 0.8f,
                      rounding = CornerRounding(radius = 0.5f)
                    )
                  }
                  val morph = remember { Morph(shape1, shape2) }
                  val interactionSource = remember { MutableInteractionSource() }
                  val isPressed by interactionSource.collectIsPressedAsState()
                  val morphProgress by animateFloatAsState(
                    targetValue = if (isPressed) 1f else 0f,
                    animationSpec = spring(),
                    label = ""
                  )
                  val pressedScale by animateFloatAsState(
                    targetValue = if (isPressed) 1.2f else 1f,
                    animationSpec = spring(),
                    label = ""
                  )
                  val buttonColor by animateColorAsState(
                    targetValue = if (isPressed) CoolOrange else CoolRed,
                    animationSpec = spring(),
                    label = ""
                  )
                  Box(
                    modifier = Modifier
                      .scale(pressedScale)
                      .size(80.dp)
                      .clip(MorphPolygonShape(morph, morphProgress))
                      .clickable(
                        interactionSource = interactionSource,
                        indication = null
                      ) { takePicture() }
                      .background(color = buttonColor)
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
            AnalysisStep(
              state = state,
              sharedTransitionScope = this@SharedTransitionLayout,
              animatedVisibilityScope = this@AnimatedContent,
              animationDuration = animationDuration,
              cornerRadius = cornerRadius,
              actions = actions
            )
          }
        }
      }
    }
  }
}

@Composable
fun FrameTimer(
  content: @Composable (Float) -> Unit
) {
  var time by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(Unit) {
    do {
      withFrameMillis {
        time += 0.01f
      }
    } while (true)
  }
  content(time)
}

@Composable
fun AnalysisStep(
  state: CameraFeature.State,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  animationDuration: Int,
  cornerRadius: Dp,
  actions: (CameraFeature.Action) -> Unit
) {
  val shader = remember { RuntimeShader(magnifyShader) }
  val rotation = remember { Animatable(0f) }
  val radius = remember { Animatable(0f) }
  val listState = rememberLazyListState()

  LaunchedEffect(Unit) {
    delay(animationDuration.toLong())
    actions(CameraFeature.Action.AnalyzePhoto(state.capturedPhoto!!))
  }

  LaunchedEffect(state.loading) {
    if (state.loading) {
      radius.animateTo(
        0.2f,
        animationSpec = tween(durationMillis = 500, easing = EaseOutQuint)
      )
    } else {
      radius.animateTo(targetValue = 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
    }
  }

  LaunchedEffect(state.messages.size) {
    listState.animateScrollToItem(0)
  }

  val caloriePosition = remember { Animatable(200f) }
  val submitPosition = remember { Animatable(200f) }
  LaunchedEffect(state.mealResponse) {
    val response = state.mealResponse
    if (response != null) {
      launch {
        caloriePosition.animateTo(0f, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy))
      }
      launch {
        delay(50)
        submitPosition.animateTo(0f, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy))
      }
    } else {
      caloriePosition.animateTo(200f, spring())
    }
  }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(color = Color.Black)
      .windowInsetsPadding(WindowInsets.systemBars)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    FrameTimer { time ->
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        state = listState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(
          items = state.messages.reversed(),
          key = { _, item -> item.id }) { index, message ->
          if (message.content[0] is MessageContent.Text) {
            val content = (message.content[0] as MessageContent.Text).text
            val isUser = message.role == "user"
            val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
            val color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
            val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
              contentAlignment = alignment
            ) {
              Surface(
                modifier = Modifier
                  .animateItem()
                  .widthIn(max = 280.dp),
                color = color,
                contentColor = textColor,
                shape = RoundedCornerShape(
                  bottomStart = 16.dp,
                  bottomEnd = 16.dp,
                  topStart = if (isUser) 16.dp else 4.dp,
                  topEnd = if (isUser) 4.dp else 16.dp,
                ),
              ) {
                Text(
                  modifier = Modifier
                    .wrapContentHeight()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                  text = content,
                )
              }
            }
          }
        }
        item {
          Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
          ) {
            with(sharedTransitionScope) {
              Image(
                modifier = Modifier
                  .sharedElement(
                    state = rememberSharedContentState("image"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                      tween(durationMillis = animationDuration, easing = EaseInOutQuint)
                    },
                  )
                  .graphicsLayer {
                    clip = true
                    rotationZ = rotation.value
                    shader.setFloatUniform(
                      "time",
                      time
                    )
                    shader.setFloatUniform(
                      "size",
                      size.width,
                      size.height
                    )
                    shader.setFloatUniform(
                      "glass",
                      0.5f,
                      0.3f
                    )
                    shader.setFloatUniform(
                      "glassRadius",
                      radius.value,
                    )
                    renderEffect = RenderEffect
                      .createRuntimeShaderEffect(
                        shader,
                        "image"
                      )
                      .asComposeRenderEffect()
                  }
                  .size(200.dp)
                  .clip(RoundedCornerShape(cornerRadius)),
                bitmap = state.capturedPhoto!!.asImageBitmap(),
                contentScale = ContentScale.Crop,
                contentDescription = "Photo"
              )
              Row(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .clip(RectangleShape),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
              ) {
                Box(
                  modifier = Modifier
                    .graphicsLayer {
                      translationY = caloriePosition.value
                    }
                    .wrapContentSize()
                    .clip(
                      shape = RoundedCornerShape(
                        topStartPercent = 50,
                        topEndPercent = 10,
                        bottomStartPercent = 50,
                        bottomEndPercent = 10
                      )
                    )
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                  Text(
                    text = "${state.mealResponse?.totalCalories} cal",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                  )
                }
                Box(
                  modifier = Modifier
                    .graphicsLayer {
                      translationY = submitPosition.value
                    }
                    .wrapContentSize()
                    .clip(
                      shape = RoundedCornerShape(
                        topStartPercent = 10,
                        topEndPercent = 50,
                        bottomStartPercent = 10,
                        bottomEndPercent = 50
                      )
                    )
                    .clickable { actions(CameraFeature.Action.LogMeal) }
                    .background(color = CoolGreen)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                  Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Accept",
                    tint = Color.White
                  )
                }
              }
            }
          }
        }
      }
    }

    AnimatedVisibility(
      state.mealResponse?.valid == true,
      enter = slideInVertically { it },
      exit = slideOutVertically { -it }
    ) {
      Composer(
        modifier = Modifier
          .imePadding()
          .fillMaxWidth(),
        value = state.contextMessage,
        onValueChange = { actions(CameraFeature.Action.UpdateContextMessage(it)) },
        onSend = { actions(CameraFeature.Action.SendContextMessage) }
      )
    }
  }
}

fun Bitmap.rotate(degrees: Int): Bitmap {
  val matrix = Matrix().apply {
    postRotate(degrees.toFloat())
  }

  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
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
    data class UpdateContextMessage(val message: String) : Action
    data object SendContextMessage : Action
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
            internalState.update { current -> current.copy(loading = false) }
          }
        }
      }

      CameraFeature.Action.LogMeal -> {
        viewModelScope.launch {
          val uri = store.saveImageLocally(state.value.capturedPhoto!!).getOrNull()
          val log = state.value.mealResponse!!.toMealLog(uri)
          db.mealLogDao().addMealLog(log)
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
          internalState.update {
            it.copy(
              loading = true,
              contextMessage = "",
              messages = messages.toList()
            )
          }

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

fun MealResponse.toMealLog(
  imageUri: String?,
  timestamp: Long = System.currentTimeMillis(),
  date: LocalDate = LocalDate.now()
): MealLog {
  return MealLog(
    foodTitle = foodTitle,
    foodDescription = foodDescription,
    totalCalories = totalCalories,
    valid = valid,
    imageUri = imageUri,
    time = timestamp,
    logDate = date
  )
}

class MorphPolygonShape(
  private val morph: Morph,
  private val percentage: Float
) : Shape {

  private val matrix = androidx.compose.ui.graphics.Matrix()
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    matrix.scale(size.width / 2f, size.height / 2f)
    matrix.translate(1f, 1f)
    val path = morph.toPath(percentage).asComposePath()
    path.transform(matrix)
    return Outline.Generic(path)
  }
}

