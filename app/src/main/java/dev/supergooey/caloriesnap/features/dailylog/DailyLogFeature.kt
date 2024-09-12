package dev.supergooey.caloriesnap.features.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.MealLogDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

interface DailyLogFeature {
  data class State(
    val logs: List<MealLog>
  ) {
    val caloriesForDay = logs.sumOf { it.totalCalories }
  }

  sealed interface Action {
    data class DeleteItem(val log: MealLog): Action
  }

  sealed class Location(val route: String) {
    data object Camera : Location("camera")
    data class Log(val id: Int) : Location("log/$id")
    data object History : Location("history")
  }
}

class DailyLogViewModel(
  private val logStore: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(DailyLogFeature.State(logs = emptyList()))
  private val today = LocalDate.now()
  private val logsFlow = logStore.mealLogDao().getMealLogsByDay(today).filterNotNull()
  val state = combine(internalState.asStateFlow(), logsFlow) { state, logsByDay ->
    state.copy(logs = logsByDay.logs)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = DailyLogFeature.State(emptyList())
  )

  fun actions(action: DailyLogFeature.Action) {
    when(action) {
      is DailyLogFeature.Action.DeleteItem -> {
        viewModelScope.launch {
          logStore.mealLogDao().deleteMealLog(action.log)
        }
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val logStore: MealLogDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return DailyLogViewModel(logStore) as T
    }
  }
}