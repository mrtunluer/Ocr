package com.yks.ocr.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yks.ocr.R
import com.yks.ocr.app.Ocr
import com.yks.ocr.databinding.FragmentCropBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CropFragment : Fragment() {

    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cropImageView.setImageBitmap(Ocr.bitmap)

        binding.backBtn.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.doneBtn.setOnClickListener {
            val croppedBitmap = binding.cropImageView.croppedImage
            croppedBitmap?.let {
                Ocr.bitmap = croppedBitmap
                findNavController().navigate(
                    R.id.action_cropFragment_to_textRecognizeFragment,
                    bundleOf("document" to null)
                )
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}