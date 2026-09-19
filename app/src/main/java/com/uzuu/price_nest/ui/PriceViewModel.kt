package com.uzuu.price_nest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.price_nest.data.PriceRepository
import com.uzuu.price_nest.data.db.Product
import com.uzuu.price_nest.data.db.Shop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShopWithProducts(
    val shop: Shop,
    val products: List<ProductUi>,
    val cheapestCount: Int = 0
)

data class ProductUi(
    val product: Product,
    val isCheapest: Boolean,
    val isMostExpensive: Boolean,
    val hasTag: Boolean
)

data class UiState(
    val shops: List<ShopWithProducts> = emptyList(),
    val searchQuery: String = "",
    val expandedShopIds: Set<Long> = emptySet(),
    val hiddenCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class PriceViewModel(private val repo: PriceRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _expandedShopIds = MutableStateFlow<Set<Long>>(emptySet())

    private val minPrices: StateFlow<Map<String, Double>> = repo.getMinPricePerProduct()
        .map { list -> list.associate { it.name to it.minPrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<UiState> = combine(
        repo.getVisibleShops(),
        minPrices,
        _searchQuery,
        _expandedShopIds,
        repo.getHiddenShopsCount()
    ) { shops, mins, query, expanded, hiddenCount ->
        shops to Triple(mins, query, expanded) to hiddenCount
    }.flatMapLatest { (pair, hiddenCount) ->
        val (shops, triple) = pair
        val (mins, query, expanded) = triple
        if (shops.isEmpty()) {
            flowOf(UiState(searchQuery = query as String, expandedShopIds = expanded as Set<Long>, hiddenCount = hiddenCount))
        } else {
            val flows = shops.map { shop ->
                repo.getProductsByShop(shop.id).map { products -> shop to products }
            }
            combine(flows) { pairs ->
                @Suppress("UNCHECKED_CAST")
                val q = query as String
                @Suppress("UNCHECKED_CAST")
                val exp = expanded as Set<Long>
                @Suppress("UNCHECKED_CAST")
                val m = mins as Map<String, Double>
                val swps = pairs.map { (shop, products) ->
                    val filtered = if (q.isBlank()) products
                    else products.filter { it.name.contains(q, ignoreCase = true) }
                    val uiProducts = filtered.map { p ->
                        ProductUi(
                            product = p,
                            isCheapest = m[p.name]?.let { p.price <= it } ?: false,
                            isMostExpensive = m[p.name]?.let { p.price > it } ?: false,
                            hasTag = p.tags.isNotBlank()
                        )
                    }
                    val cheapCount = uiProducts.count { it.isCheapest }
                    ShopWithProducts(shop, uiProducts, cheapCount)
                }.filter { it.products.isNotEmpty() || q.isBlank() }
                UiState(shops = swps, searchQuery = q, expandedShopIds = exp, hiddenCount = hiddenCount)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun toggleExpand(shopId: Long) {
        _expandedShopIds.update { ids ->
            if (ids.contains(shopId)) ids - shopId else ids + shopId
        }
    }

    fun addShop(name: String, note: String = "", tags: String = "") {
        viewModelScope.launch { repo.insertShop(Shop(name = name, note = note, tags = tags)) }
    }
    fun updateShop(shop: Shop) { viewModelScope.launch { repo.updateShop(shop) } }
    fun deleteShop(shop: Shop) { viewModelScope.launch { repo.deleteShop(shop) } }
    fun hideShop(shopId: Long) { viewModelScope.launch { repo.setShopHidden(shopId, true) } }
    fun restoreAllHidden() { viewModelScope.launch { repo.restoreAllHidden() } }

    fun addProduct(shopId: Long, name: String, price: Double, unit: String = "", note: String = "", tags: String = "") {
        viewModelScope.launch { repo.insertProduct(Product(shopId = shopId, name = name, price = price, unit = unit, note = note, tags = tags)) }
    }
    fun deleteProduct(product: Product) { viewModelScope.launch { repo.deleteProduct(product) } }
    fun updateProduct(product: Product) { viewModelScope.launch { repo.updateProduct(product) } }

    fun pasteCsv(shopId: Long, text: String) {
        viewModelScope.launch {
            val lines = text.lines().filter { it.isNotBlank() }
            val products = lines.mapNotNull { line ->
                val parts = line.split("\t", ",")
                val name = parts.getOrNull(0)?.trim() ?: return@mapNotNull null
                val price = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
                if (name.isBlank()) null
                else Product(shopId = shopId, name = name, price = price)
            }
            if (products.isNotEmpty()) repo.insertProducts(products)
        }
    }
}