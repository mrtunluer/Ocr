package com.yks.ocr.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yks.ocr.app.Ocr
import com.yks.ocr.databinding.FragmentZoomBinding
import com.yks.ocr.utils.download
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ZoomFragment : Fragment() {

    private var _binding: FragmentZoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentZoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Ocr.bitmap?.let {
            binding.zoomView.setImageBitmap(Ocr.bitmap)
        }?:binding.zoomView.download(requireContext(), Ocr.imagePath, false)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}