package com.yks.ocr.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yks.ocr.app.Ocr
import com.yks.ocr.databinding.DocumentItemBinding
import com.yks.ocr.model.Document
import com.yks.ocr.utils.download

class DocumentAdapter: RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<Document>) {
        differ.submitList(list)
    }

    fun getItem(position: Int): Document {
        return differ.currentList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            DocumentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        val context = holder.itemView.context
        val path = context.filesDir.absolutePath.plus("/${item.fileName}")

        holder.binding.apply {

            documentImg.download(context, path, true)

            titleTxt.text = item.title
            scanTxt.text = item.scanned

            holder.itemView.setOnClickListener {
                onItemClickListener?.let {
                    Ocr.imagePath = path
                    it(item)
                }
            }

        }
    }

    override fun getItemCount(): Int { return differ.currentList.size }

    inner class ViewHolder(val binding: DocumentItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var onItemClickListener: ((Document) -> Unit)? = null

    fun setOnItemClickListener(listener: (Document) -> Unit) { onItemClickListener = listener }

}