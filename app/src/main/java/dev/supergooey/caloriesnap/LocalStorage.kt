package dev.supergooey.caloriesnap

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Query("SELECT * FROM MealLog WHERE time = :time")
    suspend fun getMealLogByTime(time: Long): MealLog?

    @Query("DELETE FROM MealLog WHERE time = :time")
    suspend fun deleteMealLogByTime(time: Long)

    @Query("SELECT * FROM MealLog ORDER BY time DESC")
    fun getAllMealLogsSortedByTime(): Flow<List<MealLog>>
}

@Database(entities = [MealLog::class], version = 1, exportSchema = false)
abstract class MealLogDatabase : RoomDatabase() {
    abstract fun mealLogDao(): MealLogDao

    companion object {
        @Volatile
        private var INSTANCE: MealLogDatabase? = null

        fun getDatabase(context: Context): MealLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealLogDatabase::class.java,
                    "meal_log_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}