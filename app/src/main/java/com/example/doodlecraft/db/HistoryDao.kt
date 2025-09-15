package com.example.doodlecraft.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("SELECT * FROM drawings ORDER BY timestamp DESC")
    suspend fun getAllDrawings(): List<HistoryEntity>

    @Insert
    suspend fun insertDrawing(drawing: HistoryEntity)
}
