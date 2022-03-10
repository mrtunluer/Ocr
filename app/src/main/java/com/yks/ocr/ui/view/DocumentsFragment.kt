package com.yks.ocr.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yks.ocr.R
import com.yks.ocr.adapter.DocumentAdapter
import com.yks.ocr.app.Ocr
import com.yks.ocr.databinding.FragmentDocumentsBinding
import com.yks.ocr.model.Document
import com.yks.ocr.ui.viewmodel.DocumentViewModel
import com.yks.ocr.utils.LoadState
import com.yks.ocr.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class DocumentsFragment : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DocumentViewModel by viewModels()
    private lateinit var documentAdapter: DocumentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        viewModel.getAllDocs()
        observeDocuments()
        swipeDelete()
        clickDocument()

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun clickDocument() {
        documentAdapter.setOnItemClickListener {
            Ocr.bitmap = null
            findNavController().navigate(
                R.id.action_documentsFragment_to_textRecognizeFragment,
                bundleOf("document" to it)
            )
        }
    }

    private fun swipeDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean { return true }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val documents = documentAdapter.getItem(position)
                deleteDoc(documents.id, documents.fileName)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerView)
        }

    }

    private fun deleteDoc(id: Int, fileName: String){
        viewModel.apply {
            deleteDoc(id).also {
                deleteImgToInternalStorage(requireContext(), fileName)
                    .also {
                        requireContext().toast("Deleted")
                    }
            }
        }
    }

    private fun initAdapter() {
        documentAdapter = DocumentAdapter()
        binding.recyclerView.apply {
            adapter = documentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeDocuments() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { loadState ->
                when (loadState){
                    is LoadState.Loading -> binding.progress.visibility = View.VISIBLE
                    is LoadState.Empty -> showEmpty()
                    is LoadState.Success -> success(loadState.docs)
                    is LoadState.Error -> error()
                }
            }
        }
    }

    private fun error(){
        binding.progress.visibility = View.GONE
        binding.emptyState.emptyState.visibility = View.GONE
        requireContext().toast("Error!")
    }

    private fun showEmpty() {
        binding.progress.visibility = View.GONE
        binding.emptyState.emptyState.visibility = View.VISIBLE
        documentAdapter.submitList(emptyList())
    }

    private fun success(docs: List<Document>) {
        binding.progress.visibility = View.GONE
        binding.emptyState.emptyState.visibility = View.GONE
        documentAdapter.submitList(docs)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}