package com.yks.ocr.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yks.ocr.R
import com.yks.ocr.app.Ocr
import com.yks.ocr.databinding.FragmentHomeBinding
import com.yks.ocr.utils.Constants
import com.yks.ocr.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.lang.Exception

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnTouchListener{

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageCapture: ImageCapture
    private lateinit var camera: Camera
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var orientationEventListener: OrientationEventListener

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let { uri ->
            try {
                fixRotation(uri)
                Ocr.bitmap = getBitmapFromUri(uri)
                findNavController().navigate(R.id.action_homeFragment_to_cropFragment)
            } catch (e: IOException) {
                requireContext().toast(e.message.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraView.setOnTouchListener(this)
        requestPermissions()
        screenOrientationListener()

        binding.takeBtn.setOnClickListener {
            val bitmap = binding.cameraView.bitmap
            bitmap?.let {
                Ocr.bitmap = bitmap
                findNavController().navigate(R.id.action_homeFragment_to_cropFragment)
            }
        }

        binding.savedDocumentBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_savedDocumentsFragment)
        }

        binding.galleryImg.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.flashImg.setOnClickListener {
            onOffTorch()
        }

    }

    private fun onOffTorch(){
        if (camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(camera.cameraInfo.torchState.value == TorchState.OFF)
        }
    }

    private fun observeTorchState(){
        camera.cameraInfo.torchState.observe(viewLifecycleOwner) { torchState ->
            if (torchState == TorchState.OFF) {
                binding.flashImg.setImageResource(R.drawable.ic_baseline_flash_off_24)
            } else {
                binding.flashImg.setImageResource(R.drawable.ic_baseline_flash_on_24)
            }
        }
    }

    private fun screenOrientationListener(){
        orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                when (orientation) {
                    in 45..134 -> {
                        Ocr.orientation = 90
                    }
                    in 135..224 -> {
                        Ocr.orientation = 180
                    }
                    in 225..314 -> {
                        Ocr.orientation = 270
                    }
                    else -> {
                        Ocr.orientation = 0
                    }
                }
            }
        }
        orientationEventListener.enable()
    }

    private fun fixRotation(uri: Uri){
        Ocr.orientation = 0
        try {
            val input = requireContext().contentResolver.openInputStream(uri)
            input?.let {
                val ei = ExifInterface(input)
                val orientation: Int = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
                Ocr.orientation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    ExifInterface.ORIENTATION_NORMAL -> 0
                    else -> 0
                }
            }
        } catch (e: IOException) {
            requireContext().toast(e.message.toString())
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val imageStream = requireContext().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(imageStream)
    }

    private fun requestPermissions(){
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) startCamera()
            else requireContext().toast(getString(R.string.permission_warning))
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == false)
                requireContext().toast(getString(R.string.permission_warning))
        }
        permissionLauncher.launch(Constants.PERMISSIONS)
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { preview ->
                    preview.setSurfaceProvider(binding.cameraView.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().setFlashMode(ImageCapture.FLASH_MODE_ON).build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(viewLifecycleOwner,cameraSelector,preview,imageCapture)
                pinchZoom(camera)
                observeTorchState()
            }catch (e: Exception){
                requireContext().toast("camera failed to start")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun pinchZoom(camera: Camera){
        scaleGestureDetector = ScaleGestureDetector(requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scale = camera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                    camera.cameraControl.setZoomRatio(scale)
                    return true
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orientationEventListener.disable()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }


}