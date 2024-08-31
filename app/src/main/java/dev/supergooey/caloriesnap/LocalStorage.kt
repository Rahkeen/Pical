package dev.supergooey.caloriesnap

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMealLog(mealLog: MealLog)

  @Query("SELECT * FROM MealLog WHERE id = :id")
  suspend fun getMealLog(id: Int): MealLog

  @Update
  suspend fun updateMealLog(log: MealLog)

  @Delete
  suspend fun deleteMealLog(log: MealLog)

  @Query("SELECT * FROM MealLog ORDER BY time DESC")
  fun getMealLogsByTime(): Flow<List<MealLog>>
}

@Database(
  entities = [MealLog::class],
  version = 3,
  exportSchema = true,
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
  ]
)
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
          .fallbackToDestructiveMigrationFrom(3)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}