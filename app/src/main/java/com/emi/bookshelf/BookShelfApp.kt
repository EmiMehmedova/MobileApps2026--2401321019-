package com.emi.bookshelf

import android.app.Application
import androidx.fragment.app.Fragment
import com.emi.bookshelf.data.AppDatabase
import com.emi.bookshelf.data.BookRepository

/** Ръчен DI контейнер. */
class BookShelfApp : Application() {
    private val database by lazy { AppDatabase.create(this) }
    val repository by lazy { BookRepository(database.bookDao()) }
}

val Fragment.repository: BookRepository
    get() = (requireActivity().application as BookShelfApp).repository