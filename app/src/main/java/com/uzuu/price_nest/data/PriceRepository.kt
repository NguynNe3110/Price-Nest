package com.uzuu.price_nest.data

import com.uzuu.price_nest.data.db.AppDatabase
import com.uzuu.price_nest.data.db.Product
import com.uzuu.price_nest.data.db.Shop
import kotlinx.coroutines.flow.Flow

class PriceRepository(private val db: AppDatabase) {
    fun getVisibleShops(): Flow<List<Shop>> = db.shopDao().getVisibleShops()
    fun getAllShops(): Flow<List<Shop>> = db.shopDao().getAllShops()
    suspend fun insertShop(shop: Shop): Long = db.shopDao().insert(shop)
    suspend fun updateShop(shop: Shop) = db.shopDao().update(shop)
    suspend fun deleteShop(shop: Shop) = db.shopDao().delete(shop)
    suspend fun setShopHidden(shopId: Long, hidden: Boolean) = db.shopDao().setHidden(shopId, hidden)

    fun getProductsByShop(shopId: Long): Flow<List<Product>> = db.productDao().getProductsByShop(shopId)
    fun searchProducts(query: String): Flow<List<Product>> = db.productDao().searchProducts(query)
    fun getAllProducts(): Flow<List<Product>> = db.productDao().getAllProducts()
    fun getMinPricePerProduct(): Flow<List<com.uzuu.price_nest.data.db.ProductMinPrice>> = db.productDao().getMinPricePerProduct()
    suspend fun insertProduct(product: Product): Long = db.productDao().insert(product)
    suspend fun insertProducts(products: List<Product>) = db.productDao().insertAll(products)
    suspend fun updateProduct(product: Product) = db.productDao().update(product)
    suspend fun deleteProduct(product: Product) = db.productDao().delete(product)

    fun getHiddenShopsCount(): Flow<Int> = db.shopDao().getHiddenCount()
    suspend fun restoreAllHidden() = db.shopDao().restoreAllHidden()
}