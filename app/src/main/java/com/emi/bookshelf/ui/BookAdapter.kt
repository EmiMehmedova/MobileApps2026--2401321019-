package com.emi.bookshelf.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emi.bookshelf.R
import com.emi.bookshelf.data.Book
import com.emi.bookshelf.databinding.ItemBookBinding

class BookAdapter(
    private val onClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(DIFF) {

    class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder =
        BookViewHolder(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        with(holder.binding) {
            tvTitle.text = book.title
            tvAuthor.text = book.author
            ratingBar.rating = book.rating.toFloat()
            tvStatus.setText(if (book.isRead) R.string.read else R.string.unread)
            root.setOnClickListener { onClick(book) }
        }
    }

    fun bookAt(position: Int): Book = getItem(position)

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
        }
    }
}