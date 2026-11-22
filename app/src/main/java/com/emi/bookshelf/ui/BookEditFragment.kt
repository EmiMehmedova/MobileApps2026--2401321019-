package com.emi.bookshelf.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emi.bookshelf.R
import com.emi.bookshelf.data.Book
import com.emi.bookshelf.databinding.FragmentBookEditBinding
import com.emi.bookshelf.domain.shareText
import com.emi.bookshelf.repository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class BookEditFragment : Fragment(R.layout.fragment_book_edit) {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentBookEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookEditViewModel by viewModels { ViewModelFactory(repository) }
    private var bookId = 0L
    private var coverPath: String? = null

    private val pickCover =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) saveCover(uri)
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookEditBinding.bind(view)
        bookId = arguments?.getLong("bookId") ?: 0L
        coverPath = savedInstanceState?.getString(KEY_COVER)
        showCover()

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

        binding.btnPickCover.setOnClickListener {
            pickCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
        coverPath = book.coverPath
        showCover()
    }

    private fun showCover() {
        val path = coverPath
        if (path != null && File(path).exists()) {
            binding.imgCover.setImageURI(Uri.fromFile(File(path)))
            binding.imgCover.visibility = View.VISIBLE
        } else {
            binding.imgCover.visibility = View.GONE
        }
    }

    private fun saveCover(uri: Uri) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val path = withContext(Dispatchers.IO) { copyCover(appContext, uri) }
            if (path != null) {
                coverPath = path
                showCover()
            }
        }
    }

    private fun copyCover(
        context: Context,
        uri: Uri,
    ): String? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX_COVER_SIZE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap =
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return null
        val dir = File(context.filesDir, "covers").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return file.absolutePath
    }

    private fun readForm(): Book? {
        val title =
            binding.inputTitle.text
                ?.toString()
                .orEmpty()
                .trim()
        val author =
            binding.inputAuthor.text
                ?.toString()
                .orEmpty()
                .trim()
        binding.tilTitle.error = if (title.isEmpty()) getString(R.string.error_required) else null
        binding.tilAuthor.error = if (author.isEmpty()) getString(R.string.error_required) else null
        if (title.isEmpty() || author.isEmpty()) return null

        return Book(
            id = bookId,
            title = title,
            author = author,
            rating =
                binding.ratingBar.rating
                    .toInt()
                    .coerceIn(1, 5),
            isRead = binding.cbRead.isChecked,
            coverPath = coverPath,
        )
    }

    private fun shareBook(book: Book) {
        val context = requireContext()
        val coverFile = book.coverPath?.let { File(it) }?.takeIf { it.exists() }
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                putExtra(Intent.EXTRA_TEXT, book.shareText())
                if (coverFile != null) {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", coverFile)
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
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
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_COVER, coverPath)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val KEY_COVER = "coverPath"
        const val MAX_COVER_SIZE = 1280
    }
}
