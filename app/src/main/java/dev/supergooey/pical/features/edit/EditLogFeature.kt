package dev.supergooey.pical.features.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.pical.MealLog
import dev.supergooey.pical.MealLogDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalorieDisplay(
  val text: String = "0",
) {
  val backing = text.toIntOrNull()
  val error = backing == null || backing < 0
}
interface EditLogFeature {
  data class State(
    val id: Int = -1,
    val title: String = "",
    val calories: CalorieDisplay = CalorieDisplay(),
    val imageUri: String? = null,
    val canSave: Boolean = false,
    val finished: Boolean = false
  )

  sealed interface Action {
    data class EditTitle(val title: String) : Action
    data class EditCalories(val calories: String) : Action
    data object Save : Action
    data object Cancel : Action
  }

  sealed interface Location {
    data object Back : Location
  }
}

class EditLogViewModel(
  private val logId: Int,
  private val logDatabase: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(EditLogFeature.State(id = logId))
  val state = internalState.asStateFlow()
  private var previous: MealLog? = null

  init {
    viewModelScope.launch {
      val log = logDatabase.mealLogDao().getMealLog(logId)
      previous = log
      internalState.update {
        it.copy(
          title = log.foodTitle ?: "",
          calories = CalorieDisplay(text = log.totalCalories.toString()),
          imageUri = log.imageUri
        )
      }
    }
  }

  fun actions(action: EditLogFeature.Action) {
    when (action) {
      is EditLogFeature.Action.EditCalories -> {
        val newDisplay = CalorieDisplay(action.calories)
        val canSave = !newDisplay.error && newDisplay.backing != previous?.totalCalories
        internalState.update { prev ->
          prev.copy(
            calories = newDisplay,
            canSave = previous != null && canSave
          )
        }
      }

      is EditLogFeature.Action.EditTitle -> {
        internalState.update { prev ->
          prev.copy(
            title = action.title,
            canSave = previous != null && action.title != previous!!.foodTitle
          )
        }
      }

      EditLogFeature.Action.Save -> {
        previous?.let { log ->
          viewModelScope.launch(Dispatchers.IO) {
            val updatedLog = log.copy(
              foodTitle = state.value.title,
              totalCalories = state.value.calories.backing!!
            )
            logDatabase.mealLogDao().updateMealLog(updatedLog)
            internalState.update { it.copy(finished = true) }
          }
        }
      }

      EditLogFeature.Action.Cancel -> {
        internalState.update { it.copy(finished = true) }
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(
    private val logId: Int,
    private val logDatabase: MealLogDatabase
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return EditLogViewModel(logId, logDatabase) as T
    }
  }
}
