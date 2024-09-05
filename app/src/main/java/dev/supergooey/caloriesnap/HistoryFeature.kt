package dev.supergooey.caloriesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter

@Preview
@Composable
private fun HistoryScreenPreview() {
  CalorieSnapTheme {
    HistoryScreen(state = HistoryFeature.State())
  }
}

@Composable
fun HistoryScreen(state: HistoryFeature.State) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(8.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
    }
    items(items = state.days) { day ->
      HistoryScreenRow(day)
    }
  }
}

@Composable
fun HistoryScreenRow(state: MealLogsByDay) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 100.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(color = MaterialTheme.colorScheme.surfaceVariant)
      .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = state.day.date.format(formatter),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${state.day.totalCalories}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      state.logs.take(3).forEach { log ->
        AsyncImage(
          modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
          model = log.imageUri,
          contentScale = ContentScale.Crop,
          contentDescription = log.foodTitle,
        )
      }
    }
  }
}

interface HistoryFeature {
  data class State(
    val days: List<MealLogsByDay> = emptyList()
  )
}

class HistoryViewModel(logDao: MealLogDao) : ViewModel() {
  val state = logDao.getAllMealLogsByDay()
    .map { HistoryFeature.State(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = HistoryFeature.State()
    )

  @Suppress("UNCHECKED_CAST")
  class Factory(private val logDao: MealLogDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HistoryViewModel(logDao) as T
    }
  }
}

val formatter = DateTimeFormatter.ofPattern("MMMM d, YYYY")
