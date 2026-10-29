package com.emi.bookshelf.domain

import com.emi.bookshelf.data.Book

data class LibraryStats(
    val count: Int,
    val readCount: Int,
    val average: Double?
)

fun List<Book>.averageRating(): Double? =
    if (isEmpty()) null else map { it.rating }.average()

fun List<Book>.toStats(): LibraryStats =
    LibraryStats(size, count { it.isRead }, averageRating())

fun Book.shareText(): String {
    val stars = rating.coerceIn(0, 5)
    return "Препоръчвам ти „$title“ от $author " + "★".repeat(stars) + "☆".repeat(5 - stars)
}