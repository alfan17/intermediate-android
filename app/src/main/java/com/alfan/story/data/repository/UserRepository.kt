package com.alfan.story.data.repository

import com.alfan.story.data.response.BaseResponse
import com.alfan.story.data.response.LoginResponse
import com.alfan.story.data.services.UserServices

class UserRepository(private val userServices: UserServices) {

    suspend fun login(email: String, password: String): LoginResponse {
        return userServices.login(email, password)
    }

    suspend fun register(name:String, email: String, password: String): BaseResponse {
        return userServices.register(name, email, password)
    }
}