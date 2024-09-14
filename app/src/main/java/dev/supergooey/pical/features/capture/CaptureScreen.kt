package dev.supergooey.pical.features.capture

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.supergooey.pical.Composer
import dev.supergooey.pical.MealResponse
import dev.supergooey.pical.Message
import dev.supergooey.pical.MessageContent
import dev.supergooey.pical.R
import dev.supergooey.pical.magnifyShader
import dev.supergooey.pical.ui.theme.CoolGreen
import dev.supergooey.pical.ui.theme.CoolRed
import dev.supergooey.pical.ui.theme.MorphPolygonShape
import dev.supergooey.pical.ui.theme.PicalTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun CaptureScreenAnalyzePreview() {
  PicalTheme(darkTheme = true) {
    val bitmap = ImageBitmap.imageResource(R.drawable.bibimbap).asAndroidBitmap()
    var state by remember {
      mutableStateOf(
        CaptureFeature.State(
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
          step = CaptureFeatureStep.Analysis
        )
      )
    }
    CaptureScreen(
      state = state,
      actions = { action ->
        when (action) {
          is CaptureFeature.Action.AnalyzePhoto -> {}
          CaptureFeature.Action.SendContextMessage -> {
            state = state.copy(
              contextMessage = "",
              messages = state.messages + Message(
                role = "user",
                content = listOf(MessageContent.Text(state.contextMessage))
              )
            )
          }

          is CaptureFeature.Action.UpdateContextMessage -> {
            state = state.copy(
              contextMessage = action.message
            )
          }

          CaptureFeature.Action.LogMeal -> {}
          is CaptureFeature.Action.TakePhoto -> {}
        }
      },
      navigation = {}
    )
  }
}

@Composable
fun CaptureScreen(
  state: CaptureFeature.State,
  actions: (CaptureFeature.Action) -> Unit,
  navigation: (CaptureFeature.Location) -> Unit
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
      CaptureFeatureStep.Camera -> 0.dp
      CaptureFeatureStep.Analysis -> 12.dp
    }
  }

  fun takePicture() {
    val executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(
      executor,
      object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
          val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
          actions(CaptureFeature.Action.TakePhoto(bitmap))
        }
      }
    )
  }

  LaunchedEffect(state.finished) {
    if (state.finished) {
      navigation(CaptureFeature.Location.Back)
    }
  }

  PicalTheme(darkTheme = true) {
    SharedTransitionLayout {
      transition.AnimatedContent(
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
      ) { step ->
        when (step) {
          CaptureFeatureStep.Camera -> {
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
                    targetValue = if (isPressed) MaterialTheme.colorScheme.primary else CoolRed,
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

          CaptureFeatureStep.Analysis -> {
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
  state: CaptureFeature.State,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  animationDuration: Int,
  cornerRadius: Dp,
  actions: (CaptureFeature.Action) -> Unit
) {
  val shader = remember { RuntimeShader(magnifyShader) }
  val rotation = remember { Animatable(0f) }
  val radius = remember { Animatable(0f) }
  val listState = rememberLazyListState()

  LaunchedEffect(Unit) {
    delay(animationDuration.toLong())
    actions(CaptureFeature.Action.AnalyzePhoto(state.capturedPhoto!!))
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
                    .clickable { actions(CaptureFeature.Action.LogMeal) }
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
        onValueChange = { actions(CaptureFeature.Action.UpdateContextMessage(it)) },
        onSend = { actions(CaptureFeature.Action.SendContextMessage) }
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

