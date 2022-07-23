package com.alfan.story.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.alfan.story.data.repository.UserRepository
import com.alfan.story.data.response.BaseResponse
import com.alfan.story.data.response.LoginResponse
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

class UserViewModel(private val userRepository: UserRepository): ViewModel() {

    fun register(name: String, email: String, password: String) = liveData(Dispatchers.IO) {
        if (name.isBlank()) {
            emit(RegisterState.ErrorFullName)
        } else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
            emit(RegisterState.ErrorEmail)
        } else {
            emit(RegisterState.Loading)
            try {
                val resp = userRepository.register(name, email, password)
                if (!resp.err) {
                    emit(RegisterState.Success)
                } else {
                    emit(RegisterState.Error(message = resp.mssge))
                }
            } catch (exception: Exception) {
                try {
                    val error: HttpException = exception as HttpException
                    val errorBody: String = error.response()?.errorBody()?.string() ?: (exception as Exception).message ?: "Error Occurred!"

                    val response = Gson().fromJson(errorBody, BaseResponse::class.java)
                    emit(RegisterState.Error(response.mssge))
                } catch (e: java.lang.Exception) {
                    emit(RegisterState.Error(message = exception.message ?: "Error Occurred!"))
                }
            }
        }
    }

    fun login(email: String, password: String) = liveData(Dispatchers.IO) {
        if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
            emit(LoginState.Loading)
            try {
                emit(LoginState.SuccessLogin(userRepository.login(email, password).res!!))
            } catch (exception: Exception) {
                try {
                    val error: HttpException = exception as HttpException
                    val errorBody: String = error.response()?.errorBody()?.string() ?: (exception as Exception).message ?: "Error Occurred!"

                    val response = Gson().fromJson(errorBody, BaseResponse::class.java)
                    emit(LoginState.Error(response.mssge))
                } catch (e: java.lang.Exception) {
                    emit(LoginState.Error(message = exception.message ?: "Error Occurred!"))
                }
            }
        } else {
            emit(LoginState.ErrorEmail)
        }
    }

}

sealed class LoginState {
    object Loading: LoginState()
    data class SuccessLogin(val response: LoginResponse.Result): LoginState()
    data class Error(val message: String): LoginState()
    object ErrorEmail: LoginState()
}

sealed class RegisterState {
    object Loading: RegisterState()
    object Success: RegisterState()
    data class Error(val message: String): RegisterState()
    object ErrorFullName: RegisterState()
    object ErrorEmail: RegisterState()
}