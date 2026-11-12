package com.emi.bookshelf.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emi.bookshelf.R
import com.emi.bookshelf.databinding.FragmentBookListBinding
import com.emi.bookshelf.repository
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class BookListFragment : Fragment(R.layout.fragment_book_list) {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookListViewModel by viewModels { ViewModelFactory(repository) }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookListBinding.bind(view)

        val adapter = BookAdapter { book -> openEditor(book.id) }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        attachSwipeToDelete(adapter)

        binding.fabAdd.setOnClickListener { openEditor(0L) }

        viewModel.books.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
            binding.tvEmpty.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvAverage.text =
                if (stats.average == null) {
                    getString(R.string.no_rating)
                } else {
                    getString(
                        R.string.average_fmt,
                        String.format(Locale.getDefault(), "%.1f", stats.average),
                    )
                }
            binding.tvCounts.text = getString(R.string.counts_fmt, stats.count, stats.readCount)
        }
    }

    private fun openEditor(bookId: Long) {
        findNavController().navigate(R.id.action_list_to_edit, bundleOf("bookId" to bookId))
    }

    private fun attachSwipeToDelete(adapter: BookAdapter) {
        val vm = viewModel
        val callback =
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean = false

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int,
                ) {
                    val position = viewHolder.adapterPosition
                    if (position == RecyclerView.NO_POSITION) return
                    val book = adapter.bookAt(position)
                    vm.delete(book)
                    Snackbar
                        .make(viewHolder.itemView, R.string.deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) { vm.restore(book) }
                        .show()
                }
            }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recycler)
    }

    override fun onDestroyView() {
        binding.recycler.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
