package com.example.doodlecraft.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import androidx.room.Delete

@Dao
interface HistoryDao {
    @Query("SELECT * FROM drawings ORDER BY timestamp DESC")
    fun getAllDrawings(): List<HistoryEntity>

    @Insert
    fun insertDrawing(drawing: HistoryEntity)

    @Delete
    fun deleteDrawing(drawing: HistoryEntity)
}
