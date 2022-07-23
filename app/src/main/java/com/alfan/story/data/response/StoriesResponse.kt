package com.alfan.story.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoriesResponse(
    @SerializedName("listStory")
    var res: List<Story> = emptyList(),
): BaseResponse(), Parcelable {
    @Parcelize
    data class Story(
        @SerializedName("id")
        val idStory: String,

        @SerializedName("name")
        val name: String = "",

        @SerializedName("description")
        val desc: String = "",

        @SerializedName("photoUrl")
        val urlPhoto: String = "",

        @SerializedName("createdAt")
        val createdAt: String = "",

        @SerializedName("lat")
        val lat: Double = 0.0,

        @SerializedName("lon")
        val lon: Double = 0.0
    ): Parcelable
}