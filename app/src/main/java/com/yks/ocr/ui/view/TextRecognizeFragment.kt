package com.yks.ocr.ui.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yks.ocr.R
import com.yks.ocr.app.Ocr
import com.yks.ocr.databinding.FragmentTextRecognizeBinding
import com.yks.ocr.model.Document
import com.yks.ocr.ui.viewmodel.DocumentViewModel
import com.yks.ocr.utils.download
import com.yks.ocr.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*




@AndroidEntryPoint
class TextRecognizeFragment : Fragment() {

    private var _binding: FragmentTextRecognizeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recognizer: TextRecognizer
    private val viewModel: DocumentViewModel by viewModels()
    private val args: DocumentsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextRecognizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicks()
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val document = args.document
        if (document == null){
            processImage(setInputImage(), recognizer)
            observeScannedState()
            binding.scanImg.setImageBitmap(Ocr.bitmap)
        }else{
            getData(document)
            binding.scanImg.download(requireContext(), Ocr.imagePath, false)
        }

        binding.saveBtn.setOnClickListener {
            when {
                binding.titleTxt.text.isNullOrBlank() -> {
                    requireContext().toast("Title cannot be empty")
                }
                binding.scanTxt.text.isNullOrBlank() -> {
                    requireContext().toast("Scan cannot be empty")
                }
                else -> {
                    document?.let {
                        updateDoc(it.fileName, it.id, binding.titleTxt.text.toString(), it.scanned, System.currentTimeMillis())
                    }?:saveData(Ocr.bitmap, binding.titleTxt.text.toString())
                }
            }
        }

    }

    private fun observeScannedState(){
        viewModel.scannedText.observe(viewLifecycleOwner,{text ->
            text?.let {
                binding.scanTxt.text = it.text
            }
        })

        viewModel.scannedException.observe(viewLifecycleOwner,{ exception ->
            exception?.let {
                requireContext().toast(exception.message.toString())
            }
        })


    }

    private fun processImage(inputImage: InputImage, recognizer: TextRecognizer){
        viewModel.processImage(inputImage, recognizer)
    }

    private fun updateDoc(fileName: String, id: Int, title: String, scanned: String, time: Long){
        viewModel.updateDoc(fileName, id, title, scanned, time)
            .also {
                requireContext().toast("Updated").also {
                    findNavController().navigate(R.id.action_textRecognizeFragment_to_documentsFragment)
                }
            }
    }

    private fun clicks(){
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.scanImg.setOnClickListener {
            findNavController().navigate(R.id.action_textRecognizeFragment_to_zoomFragment)
        }
    }

    private fun getData(document: Document){
        binding.apply {
            scanTxt.text = document.scanned
            titleTxt.setText(document.title)
        }
    }

    private fun saveData(bitmap: Bitmap?, title: String){
        val fileName = UUID.randomUUID().toString().plus(".jpg")
        val imageSavedSuccessfully =
            bitmap?.let {
                viewModel.saveImgToInternalStorage(it, requireContext(), fileName)
            }
        if (imageSavedSuccessfully == true){
            viewModel.insertDoc(fileName, title, binding.scanTxt.text.toString(), System.currentTimeMillis()).also {
                requireContext().toast("Saved").also {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setInputImage(): InputImage {
        val bitmap = Ocr.bitmap
        return InputImage.fromBitmap(bitmap!!, Ocr.orientation!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}