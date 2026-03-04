package com.sansoft.harmony.fragments.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.sansoft.harmony.database.AppDatabase

class FavoritesViewModel(private val database: AppDatabase) : ViewModel() {
    fun getFavorites() = liveData {
        emit(database.favoriteSongDAO().getAll())
    }
}
