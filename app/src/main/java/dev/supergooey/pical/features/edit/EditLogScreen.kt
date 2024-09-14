package dev.supergooey.pical.features.edit

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.supergooey.pical.R
import dev.supergooey.pical.WithTextStyle
import dev.supergooey.pical.features.history.SharedTransitionPreviewHelper
import dev.supergooey.pical.ui.theme.PicalTheme

@Preview
@Composable
private fun EditLogScreenPreview() {
  SharedTransitionPreviewHelper { sharedTransitionScope, animatedVisibilityScope ->
    PicalTheme {
      EditLogScreen(
        state = EditLogFeature.State(
          title = "Big Bibimbap Energy"
        ),
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        actions = {},
        navigate = {},
      )
    }
  }
}

@Composable
fun EditLogScreen(
  state: EditLogFeature.State,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  actions: (EditLogFeature.Action) -> Unit,
  navigate: (EditLogFeature.Location) -> Unit
) {

  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(state.finished) {
    if (state.finished) {
      keyboardController?.hide()
      navigate(EditLogFeature.Location.Back)
    }
  }

  with(sharedTransitionScope) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
      Surface(modifier = Modifier.padding(paddingValues)) {
        if(state.imageUri != null) {
          Column(
            modifier = Modifier
              .imePadding()
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .sharedBounds(
                  sharedContentState = rememberSharedContentState(key = state.id),
                  animatedVisibilityScope = animatedVisibilityScope,
                  resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                  boundsTransform = { _, _ ->
                    spring(
                      stiffness = Spring.StiffnessLow,
                      dampingRatio = Spring.DampingRatioNoBouncy
                    )
                  }
                )
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              if (LocalInspectionMode.current) {
                Image(
                  modifier = Modifier
                    .height(100.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(12.dp)),
                  painter = painterResource(R.drawable.bibimbap),
                  contentScale = ContentScale.Crop,
                  contentDescription = "food"
                )
              } else {
                AsyncImage(
                  modifier = Modifier
                    .sharedElement(
                      state = rememberSharedContentState(state.imageUri),
                      animatedVisibilityScope = animatedVisibilityScope,
                      boundsTransform = { _, _ ->
                        spring(
                          stiffness = Spring.StiffnessLow,
                          dampingRatio = Spring.DampingRatioNoBouncy
                        )
                      }
                    )
                    .height(100.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(12.dp)),
                  model = state.imageUri,
                  contentScale = ContentScale.Crop,
                  contentDescription = "food"
                )
              }
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                WithTextStyle(
                  style = MaterialTheme.typography.displayMedium,
                  color = MaterialTheme.colorScheme.onSurface
                ) {
                  LogEditField(
                    modifier = Modifier.skipToLookaheadSize().wrapContentSize(),
                    value = state.title,
                    onValueChanged = { actions(EditLogFeature.Action.EditTitle(it)) },
                    hint = "Sarcastic Food Title",
                    readOnly = state.finished
                  )
                }
                WithTextStyle(
                  style = MaterialTheme.typography.displayMedium.copy(fontSize = 12.sp),
                  color = if (state.calories.error) {
                    MaterialTheme.colorScheme.onErrorContainer
                  } else {
                    MaterialTheme.colorScheme.primary
                  }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                  ) {
                    LogEditField(
                      modifier = Modifier.wrapContentSize(),
                      value = state.calories.text,
                      onValueChanged = {
                        actions(EditLogFeature.Action.EditCalories(it))
                      },
                      keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                      ),
                      hint = "100",
                      maxLines = 1,
                      readOnly = state.finished,
                      error = state.calories.error
                    )
                    Text(text = "cal")
                  }
                }
              }
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RectangleShape),
              horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
              Button(
                enabled = state.canSave,
                onClick = { actions(EditLogFeature.Action.Save) },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.primary,
                  disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                  disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
              ) {
                Text("Save")
              }
              Button(
                onClick = { actions(EditLogFeature.Action.Cancel) },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainer,
                  contentColor = MaterialTheme.colorScheme.onSurface
                )
              ) {
                Text("Cancel")
              }
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun LogEditFieldPreview() {
  val hint by remember { mutableStateOf("Title") }
  var text by remember { mutableStateOf("") }
  PicalTheme {
    WithTextStyle(
      style = MaterialTheme.typography.displayMedium,
      color = MaterialTheme.colorScheme.onErrorContainer
    ) {
      LogEditField(
        modifier = Modifier.wrapContentSize(),
        value = text,
        onValueChanged = { text = it },
        hint = hint,
        error = true
      )
    }
  }
}

@Composable
private fun LogEditField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChanged: (String) -> Unit,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  hint: String = "",
  maxLines: Int = 2,
  error: Boolean = false,
  readOnly: Boolean = false,
) {
  Surface(
    modifier = modifier.wrapContentSize(),
    shape = RoundedCornerShape(8.dp),
    color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh
  ) {
    Box(
      modifier = Modifier.padding(4.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (value.isEmpty()) {
        Text(
          text = hint,
          style = LocalTextStyle.current.copy(),
          color = LocalTextStyle.current.color.copy(alpha = 0.5f)
        )
      }
      BasicTextField(
        modifier = Modifier.wrapContentSize(),
        value = value,
        onValueChange = onValueChanged,
        textStyle = LocalTextStyle.current,
        keyboardOptions = keyboardOptions,
        maxLines = maxLines,
        readOnly = readOnly,
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.onSurface)
      )
    }
  }
}

