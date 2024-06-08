package com.example.apiconsume

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import android.util.Log

object ApiService {

    private val client = OkHttpClient()
    private const val TAG = "ApiService"

    fun uploadImage(imageFile: File, callback: (String?, List<String>?) -> Unit) {
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request: Request = Request.Builder()
            .url("http://34.101.234.211:5000/")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Upload failed: ${e.message}")
                callback(null, null) // Notify caller of failure
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d(TAG, "Response received: $responseData")
                if (responseData != null) {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val detectedImgPath = jsonResponse.getString("detected_img_path")
                        val labelsJsonArray = jsonResponse.getJSONArray("labels")
                        val labels = mutableListOf<String>()
                        for (i in 0 until labelsJsonArray.length()) {
                            labels.add(labelsJsonArray.getString(i))
                        }
                        Log.d(TAG, "Parsed labels: $labels")
                        callback(detectedImgPath, labels)
                    } catch (e: Exception) {
                        Log.e(TAG, "JSON parsing error: ${e.message}")
                        callback(null, null) // JSON parsing error
                    }
                } else {
                    Log.e(TAG, "Null response")
                    callback(null, null) // Null response
                }
            }
        })
    }
}
