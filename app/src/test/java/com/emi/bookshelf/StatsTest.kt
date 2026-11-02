package com.emi.bookshelf

import com.emi.bookshelf.data.Book
import com.emi.bookshelf.domain.averageRating
import com.emi.bookshelf.domain.shareText
import com.emi.bookshelf.domain.toStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsTest {
    private fun book(rating: Int, read: Boolean = false) =
        Book(title = "T", author = "A", rating = rating, isRead = read)

    @Test
    fun average_ofEmptyList_isNull() {
        assertNull(emptyList<Book>().averageRating())
    }

    @Test
    fun average_isCalculatedCorrectly() {
        val books = listOf(book(5), book(4), book(3))
        assertEquals(4.0, books.averageRating()!!, 0.001)
    }

    @Test
    fun stats_countsReadBooks() {
        val stats = listOf(book(5, true), book(3), book(4, true)).toStats()
        assertEquals(3, stats.count)
        assertEquals(2, stats.readCount)
    }

    @Test
    fun shareText_containsTitleAuthorAndStars() {
        val text = Book(title = "Под игото", author = "Иван Вазов", rating = 4, isRead = true).shareText()
        assertEquals("Препоръчвам ти „Под игото“ от Иван Вазов ★★★★☆", text)
    }
}