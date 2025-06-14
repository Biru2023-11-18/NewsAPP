package com.example.newsapp.DataBaseforNews.NewsData

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("status") val status : String,
    @SerializedName("totalResults") val totalResults : String? = null  ,
    @SerializedName("articles") val articles : List<Article>? = null ,

    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
)
