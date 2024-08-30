package dev.supergooey.caloriesnap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Empty.painter
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import dev.supergooey.caloriesnap.ui.theme.CoolGreen

@Preview
@Composable
private fun FoodEntryScreenPreview() {
  CalorieSnapTheme {
    FoodEntryScreen()
  }
}

@Composable
fun FoodEntryScreen() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(color = MaterialTheme.colorScheme.background)
      .padding(vertical = 16.dp, horizontal = 32.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Image of Food
    AsyncImage(
      modifier = Modifier
        .size(200.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(color = CoolGreen),
      model = null,
      contentDescription = "Food Picture"
    )

    // Food title
    Column {
      Text(
        modifier = Modifier.padding(start = 4.dp),
        text = "Title",
        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
      )
      BasicTextField(
        value = "Hello",
        onValueChange = { }
      ) { innerTextField ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(
              RoundedCornerShape(8.dp)
            )
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
          innerTextField()
        }
      }
    }
    Column {
      Text(
        modifier = Modifier.padding(start = 4.dp),
        text = "Nutrients",
        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BasicTextField(
          modifier = Modifier.weight(1f),
          value = "Hello",
          onValueChange = { }
        ) { innerTextField ->
          Box(
            modifier = Modifier
              .wrapContentHeight()
              .clip(
                RoundedCornerShape(8.dp)
              )
              .background(color = MaterialTheme.colorScheme.surfaceVariant)
              .padding(vertical = 16.dp, horizontal = 8.dp)
          ) {
            innerTextField()
          }
        }
        Text(text = "Calories")
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
      ) {
        Text(
          text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
          style = TextStyle(fontSize = 14.sp)
        )
      }
    }
  }
}