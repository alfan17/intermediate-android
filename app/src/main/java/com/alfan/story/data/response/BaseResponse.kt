package com.alfan.story.data.response

import com.google.gson.annotations.SerializedName

open class BaseResponse {
    @SerializedName("error")
    var err: Boolean = false

    @SerializedName("message")
    var mssge: String = "Success"
}