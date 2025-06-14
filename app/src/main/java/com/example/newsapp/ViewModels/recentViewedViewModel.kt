package com.example.newsapp.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.DataBaseforNews.BookmarkedNews
import com.example.newsapp.DataBaseforNews.RecentViewedNews
import com.example.newsapp.DataBaseforNews.DatabaseRepository.BookmarkedNewsRepository
import com.example.newsapp.DataBaseforNews.DatabaseRepository.RecentViewedNewsRepository
import com.example.newsapp.DataBaseforNews.NewsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class recentViewedViewModel(application : Application, cacheLimit : Int):AndroidViewModel(application) {
    private val recentViewedRepository: RecentViewedNewsRepository
    val recentViewedNews : LiveData<List<RecentViewedNews>>

    private val bookmarkRepository : BookmarkedNewsRepository
    val bookmarkNews : LiveData<List<BookmarkedNews>>

    private val _bookmarkedIds = MutableLiveData<Set<String>>()
    val bookmarkedIds : LiveData<Set<String>> get() = _bookmarkedIds

    init {

        recentViewedRepository = RecentViewedNewsRepository(NewsDatabase.getDatabase(application), cacheLimit)
        recentViewedNews = recentViewedRepository.getCachedNews()

        bookmarkRepository = BookmarkedNewsRepository((NewsDatabase.getDatabase(application)))
        bookmarkNews = bookmarkRepository.getBookmarks()

        bookmarkNews.observeForever{
             bookmarkList ->
            _bookmarkedIds.value = bookmarkList.map {
                it.id
            }.toSet()
        }
    }

    suspend fun isBookmarked(news: BookmarkedNews) : Boolean{
        return withContext(Dispatchers.IO){
            bookmarkRepository.isBookmarked(news.id)
        }
    }

    fun bookmark(news: BookmarkedNews){
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.bookmark(news)
        }
    }
    fun deleteBookmark(news: BookmarkedNews){
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.deleteBookmark(news)
        }
    }
    fun cacheNews(news : RecentViewedNews){
        viewModelScope.launch(Dispatchers.IO) {
            recentViewedRepository.cacheNews(news)
        }
    }



}