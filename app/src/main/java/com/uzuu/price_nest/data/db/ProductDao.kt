package com.uzuu.price_nest.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    fun getProductsByShop(shopId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product): Long

    @Insert
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT name, MIN(price) as minPrice FROM products GROUP BY name")
    fun getMinPricePerProduct(): Flow<List<ProductMinPrice>>
}

data class ProductMinPrice(
    val name: String,
    val minPrice: Double
)
