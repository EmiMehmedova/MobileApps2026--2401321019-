package com.emi.bookshelf.data

import kotlinx.coroutines.flow.Flow

class BookRepository(private val dao: BookDao) {
    val books: Flow<List<Book>> = dao.observeAll()

    suspend fun get(id: Long): Book? = dao.getById(id)

    /** Create (id == 0) или Update (id != 0). */
    suspend fun save(book: Book) {
        if (book.id == 0L) dao.insert(book) else dao.update(book)
    }

    suspend fun delete(book: Book) = dao.delete(book)

    /** Връща изтрита книга (за "Отмяна"). */
    suspend fun restore(book: Book) {
        dao.insert(book)
    }
}