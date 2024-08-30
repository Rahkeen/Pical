package dev.supergooey.caloriesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import dev.supergooey.caloriesnap.ui.theme.CoolGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.log

@Preview
@Composable
private fun FoodEntryScreenPreview() {
  CalorieSnapTheme {
    FoodEntryScreen(
      state = FoodEntryFeature.State()
    )
  }
}

@Composable
fun FoodEntryScreen(state: FoodEntryFeature.State) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(color = MaterialTheme.colorScheme.background)
      .windowInsetsPadding(WindowInsets.statusBars)
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
      model = state.imageUri,
      contentScale = ContentScale.Crop,
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
        value = state.title,
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
          value = "${state.calories}",
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
          text = state.description,
          style = TextStyle(fontSize = 14.sp)
        )
      }
    }
  }
}

interface FoodEntryFeature {
  data class State(
    val title: String = "",
    val calories: Int = 0,
    val description: String = "",
    val imageUri: String? = null
  )
}

class FoodEntryViewModel(
  private val logId: Int,
  private val logDatabase: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(FoodEntryFeature.State())
  val state = internalState.asStateFlow()

  init {
    viewModelScope.launch {
      val log = logDatabase.mealLogDao().getMealLog(logId)
      internalState.update {
        it.copy(
          title = log.foodTitle ?: "",
          calories = log.totalCalories,
          description = log.foodDescription ?: "",
          imageUri = log.imageUri
        )
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(
    private val logId: Int,
    private val logDatabase: MealLogDatabase
  ): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return FoodEntryViewModel(logId, logDatabase) as T
    }
  }
}
