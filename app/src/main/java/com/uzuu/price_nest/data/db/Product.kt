package com.uzuu.price_nest.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = Shop::class,
        parentColumns = ["id"],
        childColumns = ["shopId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("shopId"), Index("name")]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val name: String,
    val price: Double = 0.0,
    val note: String = "",
    val unit: String = "",
    val tags: String = ""
)
