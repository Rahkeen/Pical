package dev.supergooey.caloriesnap.features.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.MealLogDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface EditLogFeature {
  data class State(
    val title: String = "",
    val calories: Int = 0,
    val description: String = "",
    val imageUri: String? = null,
    val canSave: Boolean = false,
    val finished: Boolean = false
  )

  sealed interface Action {
    data class EditTitle(val title: String) : Action
    data class EditCalories(val calories: Int) : Action
    data object Save : Action
  }

  sealed interface Location {
    data object Back : Location
  }
}

class EditLogViewModel(
  private val logId: Int,
  private val logDatabase: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(EditLogFeature.State())
  val state = internalState.asStateFlow()
  private var previous: MealLog? = null

  init {
    viewModelScope.launch {
      val log = logDatabase.mealLogDao().getMealLog(logId)
      previous = log
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

  fun actions(action: EditLogFeature.Action) {
    when (action) {
      is EditLogFeature.Action.EditCalories -> {
        internalState.update { prev ->
          prev.copy(
            calories = action.calories,
            canSave = previous != null && action.calories != previous!!.totalCalories
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
              totalCalories = state.value.calories
            )
            logDatabase.mealLogDao().updateMealLog(updatedLog)
            internalState.update { it.copy(finished = true) }
          }
        }
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
