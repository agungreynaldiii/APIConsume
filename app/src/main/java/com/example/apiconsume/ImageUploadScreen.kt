package com.example.apiconsume

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import coil.compose.rememberImagePainter

@Composable
fun ImageUploadScreen() {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            val imageFile = remember { mutableStateOf<File?>(null) }
            var uploadedImage by remember { mutableStateOf<Painter?>(null) }
            var isLoading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var uploadedImageUrl by remember { mutableStateOf<String?>(null) }


            // Image selection launcher
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    uri?.let {
                        Glide.with(context)
                            .asBitmap()
                            .load(it)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    val file = File(context.cacheDir, "image.jpg")
                                    file.outputStream().use { out ->
                                        resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                    }
                                    imageFile.value = file
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // Handle image clearing if needed
                                }
                            })
                    }
                }

            // Image selection button
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Select Image")
            }

            // Image preview
            imageFile.value?.let { file ->
                val bitmap = BitmapFactory.decodeFile(file.path)
                val imageBitmap = bitmap.asImageBitmap()
                Image(bitmap = imageBitmap, contentDescription = null,
                    modifier = Modifier.size(200.dp))
            }


            // Upload button
            Button(onClick = {
                imageFile.value?.let { file ->
                    isLoading = true
                    error = null

                    ApiService.uploadImage(file) { response ->
                        isLoading = false
                        response?.let { responseData ->
                            // Update the uploadedImageUrl state
                            uploadedImageUrl = responseData
                        } ?: run {
                            error = "Failed to upload image"
                        }
                    }
                }
            }) {
                Text("Upload Image")
            }
            // Loading state
            if (isLoading) {
                CircularProgressIndicator()
            }

            // Error state
            error?.let {
                Text(text = it, color = Color.Red)
            }

            // Display uploaded image
            uploadedImageUrl?.let { url ->
                Image(
                    painter = rememberImagePainter(url),  // Move here
                    contentDescription = "Uploaded Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

// Function to load and display the predicted image
@Composable
fun loadImage(imageUrl: String): Painter {
    return rememberImagePainter(imageUrl)
}

