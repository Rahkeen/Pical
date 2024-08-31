package dev.supergooey.caloriesnap

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MealLog(
    @ColumnInfo("id", defaultValue = "0") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("time") val time: Long = 0L, //ms
    @ColumnInfo("food_title") val foodTitle: String? = null,
    @ColumnInfo("total_calories") val totalCalories: Int = 0,
    @ColumnInfo("food_description") val foodDescription: String? = null,
    @ColumnInfo("image_uri") val imageUri: String? = null,
    @ColumnInfo("valid") val valid: Boolean
)