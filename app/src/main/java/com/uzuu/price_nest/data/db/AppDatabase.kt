package com.uzuu.price_nest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Shop::class, Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun productDao(): ProductDao
}