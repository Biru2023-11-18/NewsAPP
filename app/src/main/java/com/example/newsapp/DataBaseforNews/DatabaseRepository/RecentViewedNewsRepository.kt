package com.example.newsapp.DataBaseforNews.DatabaseRepository

import androidx.lifecycle.LiveData
import com.example.newsapp.DataBaseforNews.RecentViewedNews
import com.example.newsapp.DataBaseforNews.NewsDatabase

class RecentViewedNewsRepository (private val newsDatabase: NewsDatabase,
                                  private val cacheLimit : Int){

    private val recentViewedNewsDao = newsDatabase.recentViewedNewsDao()

    suspend fun cacheNews(news : RecentViewedNews){
        recentViewedNewsDao.insert(news)
        recentViewedNewsDao.deleteOldEntries(cacheLimit)
    }
    fun getCachedNews() : LiveData<List<RecentViewedNews>>{
        return recentViewedNewsDao.getRecentNews(cacheLimit)
    }

}