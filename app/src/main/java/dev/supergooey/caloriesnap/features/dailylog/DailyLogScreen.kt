package dev.supergooey.caloriesnap.features.dailylog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.R
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import dev.supergooey.caloriesnap.ui.theme.CoolRed
import dev.supergooey.caloriesnap.ui.theme.MorphPolygonShape
import kotlinx.coroutines.launch

@Preview
@Composable
private fun DailyLogScreenPreview() {
  CalorieSnapTheme {
    SharedTransitionScope {
     AnimatedContent(targetState = true) { state ->
       DailyLogScreen(
         state = DailyLogFeature.State(
           listOf(
             MealLog(id = 0, foodTitle = "Item One", valid = true),
             MealLog(id = 1, foodTitle = "Item Two", valid = true),
             MealLog(id = 2, foodTitle = "Item Three", valid = true),
             MealLog(id = 3, foodTitle = "Item Four", valid = true),
             MealLog(id = 4, foodTitle = "Item Five", valid = true),
           )
         ),
         sharedTransitionScope = this@SharedTransitionScope,
         animatedVisibilityScope = this@AnimatedContent,
         action = {},
         navigate = {}
       )
     }
    }
  }
}

@Composable
fun DailyLogScreen(
  state: DailyLogFeature.State,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  action: (DailyLogFeature.Action) -> Unit,
  navigate: (DailyLogFeature.Location) -> Unit
) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    topBar = {
      PicalTopBar(state, navigate)
    },
    floatingActionButton = {
      if (LocalInspectionMode.current) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
            .clickable {},
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(R.drawable.ic_capture),
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "Capture"
          )
        }
      } else {
        val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
            .clickable {
              if (cameraPermissionState.status.isGranted) {
                navigate(DailyLogFeature.Location.Camera)
              } else {
                cameraPermissionState.launchPermissionRequest()
              }
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(R.drawable.ic_capture),
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "Capture"
          )
        }
      }
    }
  ) { paddingValues ->
    Surface(
      modifier = Modifier.padding(paddingValues),
      shape = RoundedCornerShape(
        28.dp
      )
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            Spacer(modifier = Modifier.height(0.dp))
          }
          items(items = state.logs, key = { it.id }) { log ->
            DailyLogRow3(
              modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .wrapContentHeight(),
              log = log,
              onClick = { navigate(DailyLogFeature.Location.Log(log.id)) },
              onDelete = { action(DailyLogFeature.Action.DeleteItem(log)) }
            )
          }
          item {
            Spacer(
              modifier = Modifier
                .height(80.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun PicalTopBarPreview() {
  CalorieSnapTheme {
    PicalTopBar(
      state = DailyLogFeature.State(
        logs = listOf(
          MealLog(id = 0, foodTitle = "Item One", valid = true),
          MealLog(id = 1, foodTitle = "Item Two", valid = true),
          MealLog(id = 2, foodTitle = "Item Three", valid = true),
          MealLog(id = 3, foodTitle = "Item Four", valid = true),
          MealLog(id = 4, foodTitle = "Item Five", valid = true),
        )
      ),
      navigate = {}
    )
  }
}

@Composable
private fun PicalTopBar(
  state: DailyLogFeature.State,
  navigate: (DailyLogFeature.Location) -> Unit
) {
  Row(
    modifier = Modifier
      .windowInsetsPadding(insets = WindowInsets.statusBars)
      .fillMaxWidth()
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Box(
      modifier = Modifier
        .clip(CircleShape)
        .background(color = MaterialTheme.colorScheme.primaryContainer)
        .clickable { navigate(DailyLogFeature.Location.History) }
        .padding(12.dp)
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_history),
        tint = MaterialTheme.colorScheme.secondary,
        contentDescription = ""
      )
    }
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "TODAY",
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.displaySmall
      )
      Text(
        text = "${state.caloriesForDay} cal",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.displayLarge
      )
    }
    Box(
      modifier = Modifier
        .clip(CircleShape)
        .background(color = MaterialTheme.colorScheme.primaryContainer)
        .padding(12.dp)
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_settings),
        tint = MaterialTheme.colorScheme.secondary,
        contentDescription = ""
      )
    }
  }
}

@Preview
@Composable
fun DailyLogRowOptions() {
  CalorieSnapTheme {
    val log = MealLog(
      foodTitle = "Big Bibimbap Energy",
      totalCalories = 600,
      valid = true
    )
    Surface {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
      ) {
        DailyLogRow(log = log)
        DailyLogRow2(log = log)
        DailyLogRow3(log = log)
      }
    }
  }
}

@Preview
@Composable
private fun DailyLogRowPreview() {
  CalorieSnapTheme {
    DailyLogRow(
      log = MealLog(
        foodTitle = "Mexican Rice Bowl",
        totalCalories = 1000,
        valid = true
      )
    )
  }
}

@Composable
private fun DailyLogRow(
  modifier: Modifier = Modifier,
  log: MealLog,
  onClick: () -> Unit = {}
) {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val cornerRadius by animateDpAsState(
    targetValue = if (pressed) 0.dp else 4.dp,
    animationSpec = tween(easing = EaseInOutQuint),
    label = "Row_nudge"
  )
  val nudge by animateDpAsState(
    targetValue = if (pressed) 0.dp else 4.dp,
    animationSpec = tween(easing = EaseInOutQuint),
    label = "Row_nudge"
  )
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(80.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(
        indication = ripple(color = MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource
      ) { onClick() },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(nudge)
  ) {
    Row(
      modifier = Modifier
        .weight(1f)
        .clip(
          shape = RoundedCornerShape(
            topEnd = cornerRadius,
            bottomEnd = cornerRadius
          )
        )
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (LocalInspectionMode.current) {
        Image(
          modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .clip(RoundedCornerShape(8.dp)),
          painter = painterResource(R.drawable.bibimbap),
          contentScale = ContentScale.Crop,
          contentDescription = "food"
        )
      } else {
        AsyncImage(
          modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .clip(RoundedCornerShape(8.dp)),
          model = log.imageUri,
          contentScale = ContentScale.Crop,
          contentDescription = "food"
        )

      }
      Text(
        modifier = Modifier
          .weight(1f)
          .padding(8.dp),
        textAlign = TextAlign.Start,
        text = log.foodTitle!!,
        style = MaterialTheme.typography.displayMedium,
        fontSize = 14.sp,
      )
    }
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .wrapContentWidth()
        .clip(RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius))
        .background(color = MaterialTheme.colorScheme.primaryContainer)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        modifier = Modifier.wrapContentSize(),
        text = "${log.totalCalories}",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.displayMedium,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
      Text(
        modifier = Modifier.wrapContentSize(),
        text = "cal",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.displayMedium,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Preview
@Composable
private fun DailyLogRowPreview2() {
  CalorieSnapTheme {
    DailyLogRow2(
      log = MealLog(
        foodTitle = "Mexican Rice Bowl",
        totalCalories = 600,
        valid = true
      )
    )
  }
}

@Composable
private fun DailyLogRow2(
  modifier: Modifier = Modifier,
  log: MealLog
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(80.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    if (LocalInspectionMode.current) {
      Image(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(1f, matchHeightConstraintsFirst = true)
          .clip(RoundedCornerShape(8.dp)),
        painter = painterResource(R.drawable.bibimbap),
        contentScale = ContentScale.Crop,
        contentDescription = "food"
      )
    } else {
      AsyncImage(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(1f, matchHeightConstraintsFirst = true)
          .clip(RoundedCornerShape(8.dp)),
        model = log.imageUri,
        contentScale = ContentScale.Crop,
        contentDescription = "food"
      )

    }
    Text(
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Start,
      text = log.foodTitle!!,
      style = MaterialTheme.typography.displayMedium,
      fontSize = 14.sp,
    )
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .wrapContentWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(color = MaterialTheme.colorScheme.primaryContainer)
        .padding(vertical = 8.dp, horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        modifier = Modifier.wrapContentSize(),
        text = "${log.totalCalories}",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.displayMedium,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
      Text(
        modifier = Modifier.wrapContentSize(),
        text = "cal",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.displayMedium,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@PreviewLightDark
@Composable
private fun DailyLogRowPreview3() {
  CalorieSnapTheme {
    DailyLogRow3(
      log = MealLog(
        foodTitle = "Big Bibimbap Energy",
        totalCalories = 600,
        valid = true
      )
    )
  }
}

@Composable
fun DailyLogRow3(
  modifier: Modifier = Modifier,
  log: MealLog,
  showEdit: Boolean = true,
  onClick: () -> Unit = {},
  onDelete: () -> Unit = {},
) {
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val dragOffset = remember { Animatable(0f) }
  var trigger by remember { mutableStateOf(false) }
  var progress by remember { mutableStateOf(0f) }
  val color = if (trigger) CoolRed else Color.White.copy(alpha = 0.7f)
  LaunchedEffect(trigger) {
    if (trigger) {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
  }
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(100.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(color = CoolRed.copy(alpha = 0.3f))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.25f)
        .fillMaxHeight(),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        modifier = Modifier
          .graphicsLayer {
            val outputScale = 0.5f + (0.2f * progress)
            scaleX = outputScale
            scaleY = outputScale
          }
          .size(32.dp),
        painter = painterResource(R.drawable.ic_trash_filled),
        tint = color,
        contentDescription = "Delete"
      )
    }
    Row(
      modifier = modifier
        .graphicsLayer {
          val triggerPoint = size.width * 0.25f
          translationX = minOf(dragOffset.value, triggerPoint)
          progress = (translationX / triggerPoint).coerceIn(0f, 1f)
          trigger = translationX >= triggerPoint
        }
        .fillMaxSize()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable(
          indication = ripple(color = MaterialTheme.colorScheme.secondaryContainer),
          interactionSource = interactionSource
        ) { onClick() }
        .padding(8.dp)
        .pointerInput(Unit) {
          detectHorizontalDragGestures(
            onDragStart = {},
            onDragEnd = {
              if (trigger) {
                onDelete()
              }
              scope.launch {
                dragOffset.animateTo(
                  0f,
                  animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = 0.8f
                  )
                )
              }
            },
            onHorizontalDrag = { _, dragAmount ->
              scope.launch {
                val update = dragOffset.value + dragAmount
                if (update > 0) {
                  dragOffset.snapTo(update)
                }
              }
            }
          )
        },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (LocalInspectionMode.current) {
        Image(
          modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .clip(RoundedCornerShape(12.dp)),
          painter = painterResource(R.drawable.bibimbap),
          contentScale = ContentScale.Crop,
          contentDescription = "food"
        )
      } else {
        AsyncImage(
          modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .clip(RoundedCornerShape(12.dp)),
          model = log.imageUri,
          contentScale = ContentScale.Crop,
          contentDescription = "food"
        )

      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          modifier = Modifier.wrapContentSize(),
          textAlign = TextAlign.Start,
          text = log.foodTitle!!,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.displayMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 14.sp,
        )
        Text(
          modifier = Modifier.wrapContentSize(),
          text = "${log.totalCalories} cal",
          color = MaterialTheme.colorScheme.primary,
          style = MaterialTheme.typography.displayMedium,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }

      if (showEdit) {
        val start = remember { RoundedPolygon.circle(numVertices = 6) }
        val end = remember {
          RoundedPolygon(
            numVertices = 6,
            rounding = CornerRounding(0.3f, smoothing = 1.0f)
          )
        }
        val morph = remember { Morph(start, end) }
        val morphProgress by animateFloatAsState(
          targetValue = if (pressed) 1f else 0f,
          animationSpec = spring(),
          label = "edit_shape"
        )
        Box(
          modifier = Modifier
            .align(Alignment.Bottom)
            .size(36.dp)
            .clip(shape = MorphPolygonShape(morph, morphProgress))
            .background(color = MaterialTheme.colorScheme.secondaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_edit),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = "Edit"
          )
        }
      }
    }
  }
}
