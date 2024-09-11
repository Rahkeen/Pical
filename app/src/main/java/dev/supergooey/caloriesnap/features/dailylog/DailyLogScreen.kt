package dev.supergooey.caloriesnap.features.dailylog

import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.rememberPermissionState
import dev.supergooey.caloriesnap.CameraFeature
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.R
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import dev.supergooey.caloriesnap.ui.theme.MorphPolygonShape

@Preview
@Composable
private fun DailyLogScreenPreview() {
  CalorieSnapTheme {
    DailyLogScreen(
      state = DailyLogFeature.State(
        listOf(
          MealLog(foodTitle = "Item One", valid = true),
          MealLog(foodTitle = "Item Two", valid = true),
          MealLog(foodTitle = "Item Three", valid = true),
          MealLog(foodTitle = "Item Four", valid = true),
          MealLog(foodTitle = "Item Five", valid = true),
        )
      ),
      navigate = {}
    )
  }
}

@Composable
fun DailyLogScreen(
  state: DailyLogFeature.State,
  navigate: (DailyLogFeature.Location) -> Unit
) {
  val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

  Scaffold(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    topBar = {
      Row(
        modifier = Modifier
          .windowInsetsPadding(insets = WindowInsets.statusBars)
          .fillMaxWidth()
          .height(80.dp)
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .clickable { navigate(DailyLogFeature.Location.History) }
            .padding(8.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_history),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = ""
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Today",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displayLarge
          )
          Text(
            text = "${state.caloriesForDay} cal",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
          )
        }
        Box(modifier = Modifier
          .clip(CircleShape)
          .padding(8.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_settings),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = ""
          )
        }
      }
    },
    bottomBar = {
      Row(
        modifier = Modifier
          .graphicsLayer { clip = false }
          .windowInsetsPadding(WindowInsets.navigationBars)
          .fillMaxWidth()
          .height(60.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .requiredSize(80.dp)
            .offset { IntOffset(x = 0, y = -(20.dp).roundToPx()) }
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .clickable { navigate(DailyLogFeature.Location.Camera) },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(R.drawable.ic_capture),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
          itemsIndexed(items = state.logs, key = { _, item -> item.id }) { index, log ->
            DailyLogRow3(
              modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .wrapContentHeight(),
              log = log,
              onClick = { navigate(DailyLogFeature.Location.Log(log.id)) }
            )
          }
          item {
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun DailyLogRowOptions() {
  CalorieSnapTheme {
    val log = MealLog(
      foodTitle = "Big Bibimbap Energy Is That Cool",
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

@Preview
@Composable
private fun DailyLogRowPreview3() {
  CalorieSnapTheme {
    DailyLogRow3(
      log = MealLog(
        foodTitle = "Mexican Rice Bowl and Another One and Another One and Another One",
        totalCalories = 600,
        valid = true
      )
    )
  }
}

@Composable
private fun DailyLogRow3(
  modifier: Modifier = Modifier,
  log: MealLog,
  onClick: () -> Unit = {}
) {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(100.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .clickable(
        indication = ripple(color = MaterialTheme.colorScheme.tertiaryContainer),
        interactionSource = interactionSource
      ) { onClick() }
      .padding(8.dp),
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

    val start = remember { RoundedPolygon.circle(numVertices = 6) }
    val end = remember {
      RoundedPolygon(
        numVertices = 6,
        rounding = CornerRounding(0.3f, smoothing = 1.0f)
      )
    }
    val morph = remember { Morph(start, end) }
    val progress by animateFloatAsState(
      targetValue = if (pressed) 1f else 0f,
      animationSpec = spring(),
      label = "edit_shape"
    )
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(shape = MorphPolygonShape(morph, progress))
        .background(color = MaterialTheme.colorScheme.tertiaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(R.drawable.ic_edit),
        contentDescription = "Edit"
      )
    }
  }
}
