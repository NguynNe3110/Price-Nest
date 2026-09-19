package com.uzuu.price_nest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.uzuu.price_nest.data.PriceRepository
import com.uzuu.price_nest.data.db.AppDatabase
import com.uzuu.price_nest.ui.HomeScreen
import com.uzuu.price_nest.ui.PriceViewModel
import com.uzuu.price_nest.ui.theme.PriceNestTheme

class MainActivity : ComponentActivity() {
    // ponytail: manual DI, no Hilt. Upgrade to Hilt when >3 screens or need scoped VMs.
    private lateinit var vm: PriceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(this, AppDatabase::class.java, "price_nest.db").build()
        val repo = PriceRepository(db)
        vm = PriceViewModel(repo)

        setContent {
            PriceNestTheme {
                HomeScreen(vm)
            }
        }
    }
}