package com.example.newsapp.DataBaseforNews.API

import com.example.newsapp.DataBaseforNews.NewsData.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("everything")
    suspend fun getNews(
        @Query("q") query : String ,
        @Query("from") date : String ,
        @Query("sortedBy") sortBy: String = "publishedAt" ,
        @Query("apiKey") apiKey : String = "d905b1830a8645299d1dfcea085bd7ec"
    ) : Response<NewsResponse>

    @GET("top-headlines")
    suspend fun getCategoryNews(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("apiKey") apiKey: String = "d905b1830a8645299d1dfcea085bd7ec"
    ): Response<NewsResponse>

    @GET("top-headlines")
    suspend fun getBreakingNews(

        @Query("country")country : String ,
        @Query("apiKey")apiKey: String = "d905b1830a8645299d1dfcea085bd7ec"
    ):Response<NewsResponse>
    @GET("everything")
    suspend fun getByDomain(
        @Query("domains")domains: String ,
        @Query("apiKey")apiKey: String = "d905b1830a8645299d1dfcea085bd7ec"
    ):Response<NewsResponse>



}