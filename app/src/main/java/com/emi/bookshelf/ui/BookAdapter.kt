package com.emi.bookshelf.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emi.bookshelf.R
import com.emi.bookshelf.data.Book
import com.emi.bookshelf.databinding.ItemBookBinding
import java.io.File

class BookAdapter(
    private val onClick: (Book) -> Unit,
) : ListAdapter<Book, BookAdapter.BookViewHolder>(DIFF) {
    class BookViewHolder(
        val binding: ItemBookBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BookViewHolder = BookViewHolder(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: BookViewHolder,
        position: Int,
    ) {
        val book = getItem(position)
        with(holder.binding) {
            tvTitle.text = book.title
            tvAuthor.text = book.author
            ratingBar.rating = book.rating.toFloat()
            tvStatus.setText(if (book.isRead) R.string.read else R.string.unread)
            root.setOnClickListener { onClick(book) }

            val cover = book.coverPath?.takeIf { File(it).exists() }?.let { loadThumb(it) }
            imgCover.setImageBitmap(cover)
            imgCover.visibility = if (cover != null) View.VISIBLE else View.GONE
        }
    }

    fun bookAt(position: Int): Book = getItem(position)

    private fun loadThumb(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= THUMB_SIZE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, options)
    }

    private companion object {
        const val THUMB_SIZE = 256

        val DIFF =
            object : DiffUtil.ItemCallback<Book>() {
                override fun areItemsTheSame(
                    oldItem: Book,
                    newItem: Book,
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: Book,
                    newItem: Book,
                ) = oldItem == newItem
            }
    }
}
