package com.emi.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emi.bookshelf.data.BookRepository

class ViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(BookListViewModel::class.java) ->
            BookListViewModel(repository) as T
        modelClass.isAssignableFrom(BookEditViewModel::class.java) ->
            BookEditViewModel(repository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}