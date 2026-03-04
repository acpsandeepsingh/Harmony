package com.sansoft.harmony.fragments.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sansoft.harmony.database.AppDatabase

class ViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}