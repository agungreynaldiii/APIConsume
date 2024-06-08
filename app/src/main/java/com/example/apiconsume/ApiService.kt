package com.example.apiconsume

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

object ApiService {

    private val client = OkHttpClient()

    fun uploadImage(imageFile: File, callback: (String?) -> Unit) {
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
                callback(null) // Notify caller of failure
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                callback(responseData) // Pass response data to caller
            }
        })
    }
}
