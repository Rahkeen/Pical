package dev.supergooey.caloriesnap.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.MealLogDao
import dev.supergooey.caloriesnap.MealLogsByDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface LogHistoryFeature {
  data class State(
    val days: List<MealLogsByDay> = emptyList()
  )

  sealed class Location(val route: String) {
    data class Logs(val date: String): Location(route ="logs?date=$date")
  }
}

class LogHistoryViewModel(logDao: MealLogDao) : ViewModel() {
  val state = logDao.getAllMealLogsByDay()
    .map { LogHistoryFeature.State(it) }
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

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, YYYY")
