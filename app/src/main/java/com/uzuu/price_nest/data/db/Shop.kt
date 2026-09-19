package com.uzuu.price_nest.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String = "",
    val tags: String = "",
    val isHidden: Boolean = false
)
