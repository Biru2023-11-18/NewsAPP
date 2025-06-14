package com.example.newsapp.ViewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NewsViewModelFactory(
    private val application: Application,
    private val cacheLimit: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(recentViewedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return recentViewedViewModel(application, cacheLimit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}