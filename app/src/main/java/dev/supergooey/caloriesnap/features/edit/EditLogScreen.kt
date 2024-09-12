package dev.supergooey.caloriesnap.features.edit

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.supergooey.caloriesnap.R
import dev.supergooey.caloriesnap.WithTextStyle
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme

@Preview
@Composable
private fun EditLogScreenPreview() {
  CalorieSnapTheme {
    EditLogScreen(
      state = EditLogFeature.State(
        title = "Big Bibimbap Energy"
      ),
      actions = {},
      navigate = {},
    )
  }
}

@Composable
fun EditLogScreen(
  state: EditLogFeature.State,
  actions: (EditLogFeature.Action) -> Unit,
  navigate: (EditLogFeature.Location) -> Unit
) {

  LaunchedEffect(state.finished) {
    if (state.finished) {
      navigate(EditLogFeature.Location.Back)
    }
  }

  Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
    Surface(modifier = Modifier.padding(paddingValues)) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
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
                .height(100.dp)
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(12.dp)),
              model = state.imageUri,
              contentScale = ContentScale.Crop,
              contentDescription = "food"
            )
          }
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            WithTextStyle(
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.onSurface
            ) {
              LogEditField(
                modifier = Modifier.wrapContentSize(),
                value = state.title,
                onValueChanged = { actions(EditLogFeature.Action.EditTitle(it)) },
                hint = "Sarcastic Food Title"
              )
            }
            WithTextStyle(
              style = MaterialTheme.typography.displayMedium.copy(fontSize = 12.sp),
              color = if (state.calories.error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
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
                  error = state.calories.error
                )
                Text(text = "cal")
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
  CalorieSnapTheme {
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
  error: Boolean = false
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
        maxLines = 2,
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.onSurface)
      )
    }
  }
}

