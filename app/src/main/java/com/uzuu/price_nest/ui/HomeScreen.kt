package com.uzuu.price_nest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.price_nest.data.db.Product
import com.uzuu.price_nest.data.db.Shop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: PriceViewModel) {
    val state by vm.uiState.collectAsState()
    var showAddShop by remember { mutableStateOf(false) }
    var addProductForShop by remember { mutableStateOf<Shop?>(null) }
    var pendingDeleteShop by remember { mutableStateOf<Shop?>(null) }
    var pendingDeleteProduct by remember { mutableStateOf<Product?>(null) }
    var pendingEditShop by remember { mutableStateOf<Shop?>(null) }
    var pendingEditProduct by remember { mutableStateOf<Product?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddShop = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; vm.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Tìm sản phẩm...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (state.hiddenCount > 0) {
                TextButton(
                    onClick = { showRestoreDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text("Khôi phục ${state.hiddenCount} shop ẩn", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(state.shops, key = { it.shop.id }) { swp ->
                    ShopCard(
                        shopWithProducts = swp,
                        onEdit = { pendingEditShop = swp.shop },
                        onDelete = { pendingDeleteShop = swp.shop },
                        onHide = { vm.hideShop(swp.shop.id) },
                        onAddProduct = { addProductForShop = swp.shop },
                        onEditProduct = { pendingEditProduct = it },
                        onDeleteProduct = { pendingDeleteProduct = it }
                    )
                }
            }
        }
    }

    if (showAddShop) {
        AddShopDialog(
            onDismiss = { showAddShop = false },
            onConfirm = { name: String, note: String, tags: String ->
                vm.addShop(name, note, tags)
                showAddShop = false
            }
        )
    }

    addProductForShop?.let { shop ->
        AddProductDialog(
            shopName = shop.name,
            onDismiss = { addProductForShop = null },
            onConfirm = { name: String, price: Double, unit: String, note: String, tags: String ->
                vm.addProduct(shop.id, name, price, unit, note, tags)
                addProductForShop = null
            }
        )
    }

    pendingDeleteShop?.let { shop ->
        AlertDialog(
            onDismissRequest = { pendingDeleteShop = null },
            title = { Text("Xóa shop") },
            text = { Text("Xóa shop \"${shop.name}\" và toàn bộ sản phẩm bên trong?") },
            confirmButton = { Button(onClick = { vm.deleteShop(shop); pendingDeleteShop = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Xóa") } },
            dismissButton = { TextButton(onClick = { pendingDeleteShop = null }) { Text("Hủy") } }
        )
    }

    pendingDeleteProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDeleteProduct = null },
            title = { Text("Xóa sản phẩm") },
            text = { Text("Xóa \"${product.name}\"?") },
            confirmButton = { Button(onClick = { vm.deleteProduct(product); pendingDeleteProduct = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Xóa") } },
            dismissButton = { TextButton(onClick = { pendingDeleteProduct = null }) { Text("Hủy") } }
        )
    }

    pendingEditShop?.let { shop ->
        EditShopDialog(
            shop = shop,
            onDismiss = { pendingEditShop = null },
            onConfirm = { updated -> vm.updateShop(updated); pendingEditShop = null }
        )
    }

    pendingEditProduct?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { pendingEditProduct = null },
            onConfirm = { updated -> vm.updateProduct(updated); pendingEditProduct = null }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Khôi phục shop ẩn") },
            text = { Text("Bạn có muốn khôi phục ${state.hiddenCount} shop bị ẩn?") },
            confirmButton = {
                Button(onClick = { vm.restoreAllHidden(); showRestoreDialog = false }) { Text("Khôi phục") }
            },
            dismissButton = { TextButton(onClick = { showRestoreDialog = false }) { Text("Hủy") } }
        )
    }
}

@Composable
fun ShopCard(
    shopWithProducts: ShopWithProducts,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    val swp = shopWithProducts
    val badge = if (swp.cheapestCount > 0) "🏆 ${swp.cheapestCount}" else "⚠️"
    val borderColor = if (swp.cheapestCount > 0) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${swp.shop.name} — $badge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF212121)
                    )
                    if (swp.shop.tags.isNotBlank()) {
                        Text("🏷️ ${swp.shop.tags}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFC107), fontSize = 12.sp)
                    }
                    if (swp.shop.note.isNotBlank()) {
                        Text(swp.shop.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onAddProduct) { Text("+SP", fontSize = 12.sp) }
                TextButton(onClick = onEdit) { Text("Sửa", fontSize = 12.sp) }
                TextButton(onClick = onHide) { Text("Ẩn", fontSize = 12.sp) }
                TextButton(onClick = onDelete) { Text("Xóa", color = Color.Red, fontSize = 12.sp) }
            }

            swp.products.forEach { pui ->
                ProductRow(pui, onEditProduct, onDeleteProduct)
            }
        }
    }
}

@Composable
fun ProductRow(pui: ProductUi, onEdit: (Product) -> Unit, onDelete: (Product) -> Unit) {
    val bgColor = when {
        pui.isCheapest -> Color(0x334CAF50)
        pui.isMostExpensive -> Color(0x33F44336)
        else -> Color.Transparent
    }
    val priceColor = when {
        pui.isCheapest -> Color(0xFF2E7D32)
        pui.isMostExpensive -> Color(0xFFC62828)
        else -> Color(0xFF424242)
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(pui.product.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF212121))
            Text(
                "${pui.product.price} ${pui.product.unit}".trim(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = priceColor
            )
            if (pui.product.note.isNotBlank()) {
                Text(pui.product.note, fontSize = 11.sp, color = Color.Gray)
            }
            if (pui.hasTag) {
                Text("🏷️ ${pui.product.tags}", fontSize = 11.sp, color = Color(0xFFFFC107))
            }
        }
        Row {
            TextButton(onClick = { onEdit(pui.product) }) { Text("Sửa", fontSize = 12.sp) }
            TextButton(onClick = { onDelete(pui.product) }) { Text("Xóa", color = Color.Red, fontSize = 12.sp) }
        }
    }
}

@Composable
fun AddShopDialog(onDismiss: () -> Unit, onConfirm: (name: String, note: String, tags: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Shop", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên shop *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags (cách bởi dấu phẩy)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim(), note.trim(), tags.trim()) }, enabled = name.isNotBlank()) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
fun EditShopDialog(shop: Shop, onDismiss: () -> Unit, onConfirm: (Shop) -> Unit) {
    var name by remember { mutableStateOf(shop.name) }
    var note by remember { mutableStateOf(shop.note) }
    var tags by remember { mutableStateOf(shop.tags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa Shop", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên shop *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags (cách bởi dấu phẩy)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(shop.copy(name = name.trim(), note = note.trim(), tags = tags.trim())) }, enabled = name.isNotBlank()) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
fun EditProductDialog(product: Product, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var name by remember { mutableStateOf(product.name) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var unit by remember { mutableStateOf(product.unit) }
    var note by remember { mutableStateOf(product.note) }
    var tags by remember { mutableStateOf(product.tags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa Sản Phẩm", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên SP *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Giá *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Đơn vị (cái, kg...)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            }
        },
        confirmButton = {
            val price = priceStr.toDoubleOrNull() ?: product.price
            Button(onClick = { if (name.isNotBlank()) onConfirm(product.copy(name = name.trim(), price = price, unit = unit.trim(), note = note.trim(), tags = tags.trim())) }, enabled = name.isNotBlank()) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
fun AddProductDialog(shopName: String, onDismiss: () -> Unit, onConfirm: (name: String, price: Double, unit: String, note: String, tags: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm SP vào $shopName", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên SP *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Giá *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Đơn vị (cái, kg...)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            }
        },
        confirmButton = {
            val price = priceStr.toDoubleOrNull() ?: 0.0
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim(), price, unit.trim(), note.trim(), tags.trim()) }, enabled = name.isNotBlank()) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
