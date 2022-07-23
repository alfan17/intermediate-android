package com.alfan.story.data.response

import com.google.gson.annotations.SerializedName

class LoginResponse(
    @SerializedName("loginResult")
    val res: Result? = null,
): BaseResponse() {
    data class Result (
        @SerializedName("userId")
        val idUser: String,

        @SerializedName("name")
        val name: String,

        @SerializedName("token")
        val token: String
    )
}