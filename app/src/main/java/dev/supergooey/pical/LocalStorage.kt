package dev.supergooey.pical

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@Entity
data class MealDay(
  @PrimaryKey val date: LocalDate,
  @ColumnInfo(name = "totalCalories") val totalCalories: Int = 0,
  @ColumnInfo(name = "entryCount") val entryCount: Int = 0
)

@Entity
data class MealLog(
  @ColumnInfo("id", defaultValue = "0") @PrimaryKey(autoGenerate = true) val id: Int = 0,
  @ColumnInfo("time") val time: Long = 0L,
  @ColumnInfo("log_date") val logDate: LocalDate? = null,
  @ColumnInfo("food_title") val foodTitle: String? = null,
  @ColumnInfo("total_calories") val totalCalories: Int = 0,
  @ColumnInfo("food_description") val foodDescription: String? = null,
  @ColumnInfo("image_uri") val imageUri: String? = null,
  @ColumnInfo("valid") val valid: Boolean
)

fun testMealLog(id: Int = 0, title: String = "Cool Beans"): MealLog {
  return MealLog(id = id, foodTitle = title, valid = true)
}

data class MealLogsByDay(
  @Embedded val day: MealDay,
  @Relation(
    parentColumn = "date",
    entityColumn = "log_date"
  )
  val logs: List<MealLog>
)

class FakeLogDao: MealLogDao {
  override suspend fun insertMealDay(mealDay: MealDay) {
    TODO("Not yet implemented")
  }

  override suspend fun insertMealLog(mealLog: MealLog) {
    TODO("Not yet implemented")
  }

  override suspend fun getMealLog(id: Int): MealLog {
    TODO("Not yet implemented")
  }

  override suspend fun updateMealLog(log: MealLog) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteMealLog(log: MealLog) {
    TODO("Not yet implemented")
  }

  override fun getMealLogsByDay(date: LocalDate): Flow<MealLogsByDay?> {
    TODO("Not yet implemented")
  }

  override fun getAllMealLogsByDay(): Flow<List<MealLogsByDay>> {
    TODO("Not yet implemented")
  }

  override suspend fun addMealLog(mealLog: MealLog) = Unit
}

@Dao
interface MealLogDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMealDay(mealDay: MealDay)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMealLog(mealLog: MealLog)

  @Query("SELECT * FROM MealLog WHERE id = :id")
  suspend fun getMealLog(id: Int): MealLog

  @Update
  suspend fun updateMealLog(log: MealLog)

  @Delete
  suspend fun deleteMealLog(log: MealLog)

  @Transaction
  @Query("SELECT * FROM MealDay WHERE date = :date")
  fun getMealLogsByDay(date: LocalDate): Flow<MealLogsByDay?>

  @Transaction
  @Query("SELECT * FROM MealDay ORDER BY date DESC")
  fun getAllMealLogsByDay(): Flow<List<MealLogsByDay>>

  @Transaction
  suspend fun addMealLog(mealLog: MealLog) {
    insertMealLog(mealLog)
    val mealLogsByDay = getMealLogsByDay(mealLog.logDate!!).first()
    if (mealLogsByDay == null) {
      insertMealDay(MealDay(mealLog.logDate, mealLog.totalCalories, 1))
    } else {
      val mealDay = mealLogsByDay.day
      insertMealDay(
        mealDay.copy(
          totalCalories = mealDay.totalCalories + mealLog.totalCalories,
          entryCount = mealDay.entryCount + 1
        )
      )
    }
  }
}

@Database(
  entities = [MealLog::class, MealDay::class],
  version = 4,
  exportSchema = true,
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
  ]
)
@TypeConverters(LocalDateConverter::class)
abstract class MealLogDatabase : RoomDatabase() {
  abstract fun mealLogDao(): MealLogDao

  companion object {
    @Volatile
    private var INSTANCE: MealLogDatabase? = null

    fun getDatabase(context: Context): MealLogDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room
          .databaseBuilder(
            context.applicationContext,
            MealLogDatabase::class.java,
            "meal_log_database"
          )
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

class LocalDateConverter {
  @TypeConverter
  fun epochDayToDate(epochDay: Long): LocalDate {
    return LocalDate.ofEpochDay(epochDay)
  }

  @TypeConverter
  fun dateToEpochDay(date: LocalDate): Long {
    return date.toEpochDay()
  }
}