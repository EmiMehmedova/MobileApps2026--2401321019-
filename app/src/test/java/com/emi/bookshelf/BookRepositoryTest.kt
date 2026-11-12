package com.emi.bookshelf

import com.emi.bookshelf.data.Book
import com.emi.bookshelf.data.BookDao
import com.emi.bookshelf.data.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class FakeBookDao : BookDao {
    private val items = MutableStateFlow<List<Book>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Book>> = items

    override suspend fun getById(id: Long): Book? = items.value.find { it.id == id }

    override suspend fun insert(book: Book): Long {
        val id = if (book.id == 0L) nextId++ else book.id
        items.value = items.value + book.copy(id = id)
        return id
    }

    override suspend fun update(book: Book) {
        items.value = items.value.map { if (it.id == book.id) book else it }
    }

    override suspend fun delete(book: Book) {
        items.value = items.value.filter { it.id != book.id }
    }
}

class BookRepositoryTest {
    private lateinit var repo: BookRepository

    @Before
    fun setUp() {
        repo = BookRepository(FakeBookDao())
    }

    private fun newBook(title: String = "Книга") = Book(title = title, author = "Автор", rating = 3, isRead = false)

    @Test
    fun save_newBook_isInserted() =
        runTest {
            repo.save(newBook())
            val all = repo.books.first()
            assertEquals(1, all.size)
            assertEquals(1L, all[0].id)
        }

    @Test
    fun save_existingBook_isUpdated() =
        runTest {
            repo.save(newBook())
            val saved = repo.books.first()[0]
            repo.save(saved.copy(title = "Нова", rating = 5))

            val all = repo.books.first()
            assertEquals(1, all.size)
            assertEquals("Нова", all[0].title)
            assertEquals(5, all[0].rating)
        }

    @Test
    fun delete_removesBook() =
        runTest {
            repo.save(newBook())
            repo.delete(repo.books.first()[0])
            assertEquals(0, repo.books.first().size)
            assertNull(repo.get(1L))
        }

    @Test
    fun restore_bringsBookBack() =
        runTest {
            repo.save(newBook())
            val book = repo.books.first()[0]
            repo.delete(book)
            repo.restore(book)
            assertEquals(book, repo.get(book.id))
        }
}
