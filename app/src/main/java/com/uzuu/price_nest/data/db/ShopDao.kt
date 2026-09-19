package com.uzuu.price_nest.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE isHidden = 0 ORDER BY name ASC")
    fun getVisibleShops(): Flow<List<Shop>>

    @Query("SELECT * FROM shops ORDER BY name ASC")
    fun getAllShops(): Flow<List<Shop>>

    @Insert
    suspend fun insert(shop: Shop): Long

    @Update
    suspend fun update(shop: Shop)

    @Delete
    suspend fun delete(shop: Shop)

    @Query("UPDATE shops SET isHidden = :hidden WHERE id = :shopId")
    suspend fun setHidden(shopId: Long, hidden: Boolean)

    @Query("SELECT COUNT(*) FROM shops WHERE isHidden = 1")
    fun getHiddenCount(): Flow<Int>

    @Query("UPDATE shops SET isHidden = 0 WHERE isHidden = 1")
    suspend fun restoreAllHidden()
}
