package com.alfan.story.data.services

import com.alfan.story.data.response.BaseResponse
import com.alfan.story.data.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface StoryServices {

    @GET("stories")
    suspend fun stories(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int = 1,
    ): StoriesResponse

    @Multipart
    @POST("stories")
    suspend fun create(
        @PartMap partMap: HashMap<String, RequestBody>,
        @Part file: MultipartBody.Part,
    ): BaseResponse

}