package com.alfan.story.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.alfan.story.data.repository.StoryRepository
import com.alfan.story.data.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import java.io.File

class StoryViewModel(private val storyRepository: StoryRepository): ViewModel() {

    fun getList() = storyRepository.list().cachedIn(viewModelScope)

    fun create(desc: String, latitude: String, longitude: String, photo: File) = liveData(Dispatchers.IO) {
        try {
            val response = storyRepository.create(desc, latitude, longitude, photo)

            if (!response.err) {
                emit(CreateState.Success)
            } else {
                emit(CreateState.Error(message = response.mssge))
            }
        } catch (exception: Exception) {
            try {
                val error: HttpException = exception as HttpException
                val errorBody: String = error.response()?.errorBody()?.string() ?: (exception as Exception).message ?: "Error Occurred!"

                val response = Gson().fromJson(errorBody, BaseResponse::class.java)
                emit(CreateState.Error(response.mssge))
            } catch (e: Exception) {
                emit(CreateState.Error(message = exception.message ?: "Error Occurred!"))
            }
        }

    }
}

sealed class CreateState {
    object Success: CreateState()
    data class Error(val message: String): CreateState()
}