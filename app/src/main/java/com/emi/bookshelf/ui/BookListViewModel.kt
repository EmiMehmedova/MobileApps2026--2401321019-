package com.emi.bookshelf.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.emi.bookshelf.data.Book
import com.emi.bookshelf.data.BookRepository
import com.emi.bookshelf.domain.LibraryStats
import com.emi.bookshelf.domain.toStats
import kotlinx.coroutines.launch

class BookListViewModel(private val repository: BookRepository) : ViewModel() {
    val books: LiveData<List<Book>> = repository.books.asLiveData()
    val stats: LiveData<LibraryStats> = books.map { it.toStats() }

    fun delete(book: Book) {
        viewModelScope.launch { repository.delete(book) }
    }

    fun restore(book: Book) {
        viewModelScope.launch { repository.restore(book) }
    }
}