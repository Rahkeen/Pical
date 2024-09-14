package dev.supergooey.caloriesnap.features.capture

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.caloriesnap.CameraStore
import dev.supergooey.caloriesnap.ImageSource
import dev.supergooey.caloriesnap.ImageToCalorieClient
import dev.supergooey.caloriesnap.MealLog
import dev.supergooey.caloriesnap.MealLogDatabase
import dev.supergooey.caloriesnap.MealResponse
import dev.supergooey.caloriesnap.Message
import dev.supergooey.caloriesnap.MessageContent
import dev.supergooey.caloriesnap.MessagesRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.time.LocalDate

enum class CaptureFeatureStep {
  Camera,
  Analysis
}

interface CaptureFeature {
  data class State(
    val capturedPhoto: Bitmap? = null,
    val mealResponse: MealResponse? = null,
    val messages: List<Message> = emptyList(),
    val contextMessage: String = "",
    val step: CaptureFeatureStep = CaptureFeatureStep.Camera,
    val loading: Boolean = false,
    val finished: Boolean = false
  )

  sealed interface Action {
    data class TakePhoto(val bitmap: Bitmap) : Action
    data class AnalyzePhoto(val bitmap: Bitmap) : Action
    data object LogMeal : Action
    data class UpdateContextMessage(val message: String) : Action
    data object SendContextMessage : Action
  }

  enum class Location {
    Back
  }
}

class CaptureViewModel(
  private val store: CameraStore,
  private val db: MealLogDatabase
) : ViewModel() {
  private val internalState = MutableStateFlow(CaptureFeature.State())
  val state = internalState.asStateFlow()

  fun actions(action: CaptureFeature.Action) {
    when (action) {
      is CaptureFeature.Action.TakePhoto -> {
        Log.d("Camera", "Saved Photo: ${action.bitmap}")
        viewModelScope.launch {
          internalState.update {
            it.copy(
              capturedPhoto = action.bitmap,
              step = CaptureFeatureStep.Analysis
            )
          }
        }
      }

      is CaptureFeature.Action.AnalyzePhoto -> {
        internalState.update { it.copy(loading = true) }
        val messages = mutableListOf<Message>()
        val initialMessage =
          Message(
            role = "user",
            content = listOf(
              MessageContent.Image(
                source = ImageSource(
                  data = action.bitmap.toBase64()
                )
              )
            )
          )
        messages.add(initialMessage)
        viewModelScope.launch {
          // Send it to Claude
          val response = ImageToCalorieClient.api.getMessages(MessagesRequest(messages = messages))
          Log.d("Camera", "Claude Response: $response")

          if (response.isSuccessful) {
            val messagesResponse = response.body()!!
            val meal = Json.decodeFromString<MealResponse>(
              messagesResponse
                .content
                .filterIsInstance<MessageContent.Text>()
                .first()
                .text
            )
            val mealMessage = Message(
              role = messagesResponse.role,
              content = listOf(
                MessageContent.Text(text = meal.foodDescription)
              )
            )
            messages.add(mealMessage)
            internalState.update { current ->
              current.copy(
                loading = false,
                mealResponse = meal,
                messages = messages.toList()
              )
            }
          } else {
            Log.d("Camera", "Claude Error Response: ${response.errorBody()?.string()}")
            internalState.update { current -> current.copy(loading = false) }
          }
        }
      }

      CaptureFeature.Action.LogMeal -> {
        viewModelScope.launch {
          val uri = store.saveImageLocally(state.value.capturedPhoto!!).getOrNull()
          val log = state.value.mealResponse!!.toMealLog(uri)
          db.mealLogDao().addMealLog(log)
          internalState.update { it.copy(finished = true) }
        }
      }

      CaptureFeature.Action.SendContextMessage -> {
        viewModelScope.launch {
          val message = Message(
            role = "user",
            content = listOf(
              MessageContent.Text(text = internalState.value.contextMessage)
            )
          )
          val messages = internalState.value.messages.toMutableList().apply {
            add(message)
          }
          internalState.update {
            it.copy(
              loading = true,
              contextMessage = "",
              messages = messages.toList()
            )
          }

          val response = ImageToCalorieClient.api.getMessages(MessagesRequest(messages = messages))

          if (response.isSuccessful) {
            val messagesResponse = response.body()!!
            Log.d("Camera", "Claude Context Response: $messagesResponse")
            val meal = Json.decodeFromString<MealResponse>(
              messagesResponse
                .content
                .filterIsInstance<MessageContent.Text>()
                .first()
                .text
            )
            val mealMessage = Message(
              role = messagesResponse.role,
              content = listOf(
                MessageContent.Text(text = meal.foodDescription)
              )
            )
            messages.add(mealMessage)
            internalState.update { current ->
              current.copy(
                loading = false,
                mealResponse = meal,
                messages = messages.toList()
              )
            }
          } else {
            Log.d("Camera", "Claude Error Response: ${response.errorBody()?.string()}")
            internalState.update { it.copy(loading = false) }
          }
        }
      }

      is CaptureFeature.Action.UpdateContextMessage -> {
        internalState.update { it.copy(contextMessage = action.message) }
      }
    }
  }

  private fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val store: CameraStore, private val db: MealLogDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CaptureViewModel(store, db) as T
    }
  }
}

fun MealResponse.toMealLog(
  imageUri: String?,
  timestamp: Long = System.currentTimeMillis(),
  date: LocalDate = LocalDate.now()
): MealLog {
  return MealLog(
    foodTitle = foodTitle,
    foodDescription = foodDescription,
    totalCalories = totalCalories,
    valid = valid,
    imageUri = imageUri,
    time = timestamp,
    logDate = date
  )
}
