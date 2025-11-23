package cl.duoc.fastfingers.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.fastfingers.model.WordEntity

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT text FROM words ORDER BY RANDOM() LIMIT 1")
    fun getRandomWord(): String?
}