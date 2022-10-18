package com.iman.submission1_storyapp.network

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("token")
    val token: String
)
