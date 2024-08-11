package com.android.fishq

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import retrofit2.Call
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ScanFragment : Fragment() {

    private lateinit var imageView21: ImageView
    private lateinit var view_fresh: TextView
    private lateinit var view_no_fresh: TextView
    private lateinit var view_total: TextView
    private lateinit var dialog: Dialog
    private lateinit var dialog_nodetect: Dialog

    // Gallery
    private lateinit var galleryIV: ImageView

    // Camera
    private lateinit var captureIV: ImageView
    private lateinit var imageUrl: Uri

    // Bitmap
    private lateinit var bitmap: Bitmap

    // Mode
    private var selectedMode: String = "beku"

    // Permission
    private val GALLERY_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_REQUEST_CODE = 101


    private val galleryContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val startTime = System.currentTimeMillis()
            if (uri!= null) {
                var inputStream: InputStream? = null
                try {
                    inputStream = requireContext().contentResolver.openInputStream(uri)
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    val resizedBitmap = resizeImage(bitmap, 850, 850) // Set your desired max width and height
                    galleryIV.setImageBitmap(resizedBitmap)
                } catch (e: Exception) {
                    Log.e("Error", "Error loading image", e)
                } finally {
                    inputStream?.close()
                }
            } else {
                Log.w("Warning", "No image selected")
            }
            val endTime = System.currentTimeMillis()
            val timeTaken = endTime - startTime
            Log.d("Response Time", "Time taken to load image: $timeTaken ms")
        }

    private val contract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val startTime = System.currentTimeMillis()
            if (success) {
                bitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUrl)
                val resizedBitmap =
                    resizeImage(bitmap, 850, 850) // Set your desired max width and height
                captureIV.setImageBitmap(resizedBitmap)
            }
            val endTime = System.currentTimeMillis()
            val timeTaken = endTime - startTime
            Log.d("Response Time", "Time taken to load image: $timeTaken ms")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        imageUrl = createImageUri()
    }

    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        // Initialize imageView21
        imageView21 = view.findViewById(R.id.imageView21)
        view_fresh = view.findViewById(R.id.view_fresh)
        view_no_fresh = view.findViewById(R.id.view_no_fresh)
        view_total = view.findViewById(R.id.view_total)

        galleryIV = view.findViewById(R.id.imageView21)
        val galleryImgBtn = view.findViewById<ImageButton>(R.id.gallery)
        galleryImgBtn.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(requireContext(), permission)!= PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), GALLERY_PERMISSION_REQUEST_CODE)
            } else {
                galleryContract.launch("image/*")
                view_fresh.text = "-"
                view_no_fresh.text = "-"
                view_total.text = "-"
            }
        }

        captureIV = view.findViewById(R.id.imageView21)
        val captureImgBtn = view.findViewById<ImageButton>(R.id.camera)
        captureImgBtn.setOnClickListener {
            if (checkSelfPermission(requireContext(), Manifest.permission.CAMERA)!= PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                contract.launch(imageUrl)
                view_fresh.text = "-"
                view_no_fresh.text = "-"
                view_total.text = "-"
            }
        }

        val modeSwitch = view.findViewById<MaterialSwitch>(R.id.mode_switch)
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectedMode = if (isChecked) "tidak_beku" else "beku"
        }

        val uploadButton = view.findViewById<ImageButton>(R.id.imageButton3)
        uploadButton.setOnClickListener {
            val startTime = System.currentTimeMillis()
            if (selectedMode == "") {
                Toast.makeText(
                    requireContext(),
                    "Pilih mode terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if ((!::bitmap.isInitialized)) {
                Toast.makeText(
                    requireContext(),
                    "Masukkan gambar terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.loading_popup)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            uploadImage(selectedMode, resizeImage(bitmap, 850, 850), "file")
            val endTime = System.currentTimeMillis()
            val timeTaken = endTime - startTime
            Log.d("Response Time", "Time taken to upload image: $timeTaken ms")
        }
        return view
    }

    private fun createImageUri(): Uri {
        val image = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "capture_${System.currentTimeMillis()}.png"
        )
        return FileProvider.getUriForFile(
            requireContext(),
            "com.android.fishq.FileProvider",
            image
        )
    }

    private fun resizeImage(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = Math.min(maxWidth.toFloat() / source.width, maxHeight.toFloat() / source.height)
        val width = Math.round(ratio * source.width)
        val height = Math.round(ratio * source.height)

        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private fun uploadImage(mode: String, image: Bitmap, source: String) {
        // Convert the Bitmap to a ByteArray
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageData = outputStream.toByteArray()

        // Create a MultipartBody.Part for the image
        val imagePart = createImagePart(source, imageData)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call: Call<ObjectDetectionResponse> =
                    ObjectDetectionService().detectObjects(selectedMode, imagePart)
                val response = call.execute()
                Log.d("ObjectDetection", "Response: $response")

                // Handle the response and update the ImageView
                withContext(Dispatchers.Main) {
                    val startTime = System.currentTimeMillis()
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        if (responseData != null) {
                            val imageBitmap = base64StringToBitmap(responseData.image_data)
                            val freshText = responseData.fresh.toString()
                            val nofreshText = responseData.no_fresh.toString()
                            val totalText = responseData.total.toString()
                            // Update the UI on the main thread
                            withContext(Dispatchers.Main) {
                                // Set the imageView21 with the new Bitmap
                                imageView21.setImageBitmap(imageBitmap)
                                view_fresh.text = freshText
                                view_no_fresh.text = nofreshText
                                view_total.text = totalText

                                if (totalText == "0") {
                                    dialog_nodetect = Dialog(requireContext())
                                    dialog_nodetect.setContentView(R.layout.nodetect_dialog)
                                    dialog_nodetect.window!!.setBackgroundDrawable(ColorDrawable(0))
                                    dialog_nodetect.show()
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        dialog_nodetect.dismiss()
                                    }, 3000)
                                }
                                val endTime = System.currentTimeMillis()
                                val timeTaken = endTime - startTime
                                Log.d("Response Time", "Time taken to response: $timeTaken ms")
                            }
                        }
                    } else {
                        showErrorToast("API Tidak dapat terkoneksi")
                    }
                }
            } catch (e: UnknownHostException) {
                showErrorToast("Tidak ada koneksi internet")
            } catch (e: SocketTimeoutException) {
                showErrorToast("Waktu koneksi habis. Silakan coba lagi")
            } catch (e: IOException) {
                Log.e("ObjectDetection", "Error: $e")
                showErrorToast("Kesalahan saat menyambung ke server")
            } catch (e: JsonSyntaxException) {
                Log.e("ObjectDetection", "Error: $e")
                showErrorToast("Kesalahan parsing response")
            } catch (e: Exception) {
                Log.e("ObjectDetection", "Error: $e")
                showErrorToast("Terjadi kesalahan tidak terduga")
            } finally {
                dialog.dismiss()
            }
        }
    }

    private suspend fun showErrorToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImagePart(source: String, imageData: ByteArray): MultipartBody.Part {
        val requestBody = RequestBody.create("image/*".toMediaType(), imageData)
        return MultipartBody.Part.createFormData("file", "$source.jpg", requestBody)
    }

    private fun base64StringToBitmap(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            GALLERY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleryContract.launch("image/*")
                } else {
                    showPermissionDeniedDialog("Gallery permission denied", "Please allow access to your gallery to continue.")
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contract.launch(imageUrl)
                } else {
                    showPermissionDeniedDialog("Camera permission denied", "Please allow access to your camera to continue.")
                }
            }
        }
    }

    private fun showPermissionDeniedDialog(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .show()
    }
}