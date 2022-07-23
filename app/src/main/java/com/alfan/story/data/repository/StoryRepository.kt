package com.alfan.story.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.alfan.story.data.response.BaseResponse
import com.alfan.story.data.response.StoriesResponse
import com.alfan.story.data.services.StoryServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(private val storyServices: StoryServices) {

    fun list(): LiveData<PagingData<StoriesResponse.Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 4
            ),
            pagingSourceFactory = {
                StoryPaging(storyServices)
            }
        ).liveData
    }

    suspend fun create(
        desc: String,
        latitude: String,
        longitude: String,
        file: File
    ): BaseResponse {
        val params = HashMap<String, RequestBody>()
        params["description"] = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        params["lat"] = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
        params["lon"] = longitude.toRequestBody("text/plain".toMediaTypeOrNull())
        val photo = MultipartBody.Part.createFormData("photo", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))

        return storyServices.create(params, photo)
    }

    class StoryPaging(private val service: StoryServices): PagingSource<Int, StoriesResponse.Story>()  {

        override fun getRefreshKey(state: PagingState<Int, StoriesResponse.Story>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoriesResponse.Story> {
            return try {
                val page = params.key ?: 1
                val response = service.stories(page, params.loadSize)

                LoadResult.Page(
                    data = response.res,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.res.isNullOrEmpty()) null else page + 1
                )
            } catch (exception: Exception) {
                return LoadResult.Error(exception)
            }
        }
    }
}