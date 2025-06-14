package com.example.newsapp.DataBaseforNews

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recentViewed_news")
data class RecentViewedNews(
    @PrimaryKey val id : String ,
    val title : String ,
    val description : String? ,
    val content : String ?,
    val publishedAt : String ,
    val source : String ,
    val image : String? ,
    val category : String ,
    val author : String?,
    @ColumnInfo(name = "timestamp") val timestamp : Long = System.currentTimeMillis()
)
