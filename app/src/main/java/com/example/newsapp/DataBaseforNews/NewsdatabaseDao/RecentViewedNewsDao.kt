package com.example.newsapp.DataBaseforNews.NewsdatabaseDao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.DataBaseforNews.RecentViewedNews

@Dao
interface RecentViewedNewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news : RecentViewedNews)

    @Query("SELECT * FROM recentViewed_news ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNews(limit: Int): LiveData<List<RecentViewedNews>>

    @Query("DELETE FROM recentViewed_news WHERE timestamp NOT IN (SELECT timestamp FROM recentViewed_news ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun deleteOldEntries(limit: Int)
}
