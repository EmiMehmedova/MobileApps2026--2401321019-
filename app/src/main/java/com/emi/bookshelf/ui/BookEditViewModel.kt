package com.emi.bookshelf.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emi.bookshelf.data.Book
import com.emi.bookshelf.data.BookRepository
import kotlinx.coroutines.launch

class BookEditViewModel(private val repository: BookRepository) : ViewModel() {
    private val _book = MutableLiveData<Book?>()
    val book: LiveData<Book?> = _book

    private val _done = MutableLiveData(false)
    val done: LiveData<Boolean> = _done

    fun load(id: Long) {
        if (_book.value != null) return
        viewModelScope.launch { _book.value = repository.get(id) }
    }

    fun save(book: Book) {
        viewModelScope.launch {
            repository.save(book)
            _done.value = true
        }
    }

    fun delete(book: Book) {
        viewModelScope.launch {
            repository.delete(book)
            _done.value = true
        }
    }
}