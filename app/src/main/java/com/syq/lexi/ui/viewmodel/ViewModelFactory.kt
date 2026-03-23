package com.syq.lexi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syq.lexi.data.repository.WordbookRepository

class WordbookViewModelFactory(private val repository: WordbookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordbookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordbookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
