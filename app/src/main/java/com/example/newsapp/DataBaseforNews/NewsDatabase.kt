package com.example.newsapp.DataBaseforNews

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.newsapp.DataBaseforNews.NewsdatabaseDao.BookmarkedNewsDao
import com.example.newsapp.DataBaseforNews.NewsdatabaseDao.RecentViewedNewsDao

@Database(
    entities = [RecentViewedNews::class , BookmarkedNews::class] ,
    version = 2,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase(){
    abstract fun recentViewedNewsDao() : RecentViewedNewsDao
    abstract fun bookmarkedNewsDao() : BookmarkedNewsDao

    companion object{
        @Volatile
        private var INSTANCE : NewsDatabase? = null

        fun getDatabase(context: Context):NewsDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java ,
                    "news_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}

