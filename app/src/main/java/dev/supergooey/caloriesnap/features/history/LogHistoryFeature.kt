package dev.supergooey.caloriesnap.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.MealLogDao
import dev.supergooey.caloriesnap.MealLogsByDay
import dev.supergooey.caloriesnap.features.dailylog.isToday
import dev.supergooey.caloriesnap.features.dailylog.toDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter



data class HistoryRowState(
  val dayDisplay: String,
  val dayCalories: Int,
  val isToday: Boolean,
  val date: LocalDate,
  val logs: List<MealLog>
)

interface LogHistoryFeature {
  data class State(
    val rows: List<HistoryRowState> = emptyList()
  )

  sealed class Location(val route: String) {
    data class Logs(val date: String): Location(route ="logs?date=$date")
  }
}

class LogHistoryViewModel(logDao: MealLogDao) : ViewModel() {
  val state = logDao.getAllMealLogsByDay()
    .map {
      LogHistoryFeature.State(
        rows = it.toHistoryRows()
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = LogHistoryFeature.State()
    )

  @Suppress("UNCHECKED_CAST")
  class Factory(private val logDao: MealLogDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return LogHistoryViewModel(logDao) as T
    }
  }
}

fun List<MealLogsByDay>.toHistoryRows(): List<HistoryRowState> {
  return this.map { dayEntry ->
    HistoryRowState(
      dayDisplay = dayEntry.day.date.toDisplay(),
      dayCalories = dayEntry.day.totalCalories,
      isToday = dayEntry.day.date.isToday(),
      date = dayEntry.day.date,
      logs = dayEntry.logs
    )
  }
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, YYYY")
