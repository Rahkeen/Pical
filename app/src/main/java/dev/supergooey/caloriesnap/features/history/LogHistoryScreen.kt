package dev.supergooey.caloriesnap.features.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.supergooey.caloriesnap.MealDay
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.MealLogsByDay
import dev.supergooey.caloriesnap.R
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import java.time.LocalDate

@Preview
@Composable
private fun LogHistoryScreenPreview() {
  CalorieSnapTheme {
    LogHistoryScreen(state = LogHistoryFeature.State())
  }
}

@Composable
fun LogHistoryScreen(state: LogHistoryFeature.State) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.surface
  ) { paddingValues ->
    Surface(
      modifier = Modifier.padding(paddingValues),
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(items = state.days) { day ->
          HistoryScreenRow(day)
        }
      }
    }
  }
}

@Preview
@Composable
private fun HistoryScreenRowPreview() {
  CalorieSnapTheme {
    HistoryScreenRow(
      state = MealLogsByDay(
        day = MealDay(
          date = LocalDate.parse("2024-09-12"),
          totalCalories = 2000,
          entryCount = 3
        ),
        logs = listOf(
          MealLog(id = 0, valid = true),
          MealLog(id = 1, valid = true),
          MealLog(id = 2, valid = true),
        )
      )
    )
  }
}

@Composable
fun HistoryScreenRow(state: MealLogsByDay) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 160.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        modifier = Modifier.padding(top = 16.dp),
        text = state.day.date.format(formatter),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 16.sp
      )
      Row(
        modifier = Modifier
          .graphicsLayer {
            translationY = size.height * 0.4f
          },
        horizontalArrangement = Arrangement.spacedBy((-32).dp)
      ) {
        state.logs.take(3).forEachIndexed { index, log ->
          val rotation = remember(index) { (-10..10).random().toFloat() }
          if (LocalInspectionMode.current) {
            Image(
              modifier = Modifier
                .graphicsLayer {
                  rotationZ = rotation
                }
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
              painter = painterResource(R.drawable.bibimbap),
              contentScale = ContentScale.Crop,
              contentDescription = log.foodTitle,
            )
          } else {
            AsyncImage(
              modifier = Modifier
                .graphicsLayer {
                  rotationZ = rotation
                }
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
              model = log.imageUri,
              contentScale = ContentScale.Crop,
              contentDescription = log.foodTitle,
            )
          }
        }
      }
    }
  }
}

