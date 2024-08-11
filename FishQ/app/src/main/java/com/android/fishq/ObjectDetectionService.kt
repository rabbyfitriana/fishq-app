package com.android.fishq

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ObjectDetectionService {
    @Multipart
    @POST("img_object_detection")
    fun detectObjects(
        @Query("mode") mode: String,
        @Part imagePart: MultipartBody.Part
    ): Call<ObjectDetectionResponse>

    companion object {
        operator fun invoke(): ObjectDetectionService {
            return Retrofit.Builder()
                .baseUrl("https://fishq-lpsxny64fq-as.a.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ObjectDetectionService::class.java)
        }
    }
}