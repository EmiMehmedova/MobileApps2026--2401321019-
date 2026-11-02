package com.emi.bookshelf.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.emi.bookshelf.R
import com.emi.bookshelf.data.Book
import com.emi.bookshelf.databinding.FragmentBookEditBinding
import com.emi.bookshelf.domain.shareText
import com.emi.bookshelf.repository
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BookEditFragment : Fragment(R.layout.fragment_book_edit) {
    private var _binding: FragmentBookEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookEditViewModel by viewModels { ViewModelFactory(repository) }
    private var bookId = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookEditBinding.bind(view)
        bookId = arguments?.getLong("bookId") ?: 0L

        if (bookId != 0L) {
            binding.btnDelete.visibility = View.VISIBLE
            viewModel.load(bookId)
        }

        viewModel.book.observe(viewLifecycleOwner) { book ->
            if (book != null && savedInstanceState == null) fill(book)
        }
        viewModel.done.observe(viewLifecycleOwner) { done ->
            if (done) findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener { readForm()?.let { viewModel.save(it) } }
        binding.btnShare.setOnClickListener { readForm()?.let { shareBook(it) } }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun fill(book: Book) {
        binding.inputTitle.setText(book.title)
        binding.inputAuthor.setText(book.author)
        binding.ratingBar.rating = book.rating.toFloat()
        binding.cbRead.isChecked = book.isRead
    }

    private fun readForm(): Book? {
        val title = binding.inputTitle.text?.toString().orEmpty().trim()
        val author = binding.inputAuthor.text?.toString().orEmpty().trim()
        binding.tilTitle.error = if (title.isEmpty()) getString(R.string.error_required) else null
        binding.tilAuthor.error = if (author.isEmpty()) getString(R.string.error_required) else null
        if (title.isEmpty() || author.isEmpty()) return null

        return Book(
            id = bookId,
            title = title,
            author = author,
            rating = binding.ratingBar.rating.toInt().coerceIn(1, 5),
            isRead = binding.cbRead.isChecked
        )
    }

    private fun shareBook(book: Book) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            putExtra(Intent.EXTRA_TEXT, book.shareText())
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser)))
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_msg)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.book.value?.let { viewModel.delete(it) }
            }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}