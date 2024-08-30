package dev.supergooey.caloriesnap

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class MealLog(
    @PrimaryKey
    @SerialName("time")
    val time: Long = 0L, //ms
    @SerialName("food_title") val foodTitle: String? = null,
    @SerialName("total_calories") val totalCalories: Int = 0,
    @SerialName("food_description") val foodDescription: String? = null,
    @SerialName("image_uri") val imageUri: String? = null,
    @SerialName("valid") val valid: Boolean
)