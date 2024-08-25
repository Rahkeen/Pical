package dev.supergooey.caloriesnap

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class MealLog(
    @PrimaryKey
    @Serializable
    @SerialName("time")
    val time: Long = 0L, //ms
    @Serializable
    @SerialName("total_calories")
    val totalCalories: Int = 0,
    @Serializable
    @SerialName("food_description")
    val foodDescription: String? = null,
    @Serializable
    @SerialName("image_uri")
    val imageUri: String? = null,
    @Serializable
    @SerialName("valid")
    val valid: Boolean
){

}