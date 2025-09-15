package com.example.doodlecraft.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val timestamp: Long
)
