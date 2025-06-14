package com.example.newsapp.NewsFragment

import News
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.Adapter.GeneralAdapter
import com.example.newsapp.Adapter.RecommendedAdapter
import com.example.newsapp.DataBaseforNews.BookmarkedNews
import com.example.newsapp.DataBaseforNews.RecentViewedNews
import com.example.newsapp.HorizontalItemDecoration
import com.example.newsapp.NewsActivity
import com.example.newsapp.ViewModels.recentViewedViewModel
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.ViewModels.NewsViewModelFactory
import com.example.newsapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private val viewModel : NewsViewModel by activityViewModels()
    private lateinit var generalAdapter: GeneralAdapter
    private lateinit var recommendedAdapter: RecommendedAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cacheLimit = 10
        val cacheViewModel = ViewModelProvider(this , NewsViewModelFactory(requireActivity().application , cacheLimit))
            .get(recentViewedViewModel::class.java)

        val recommendedRecycler : RecyclerView = binding.recommendedRecyclerView
        val itemsToShow = 2.5f
        recommendedRecycler.layoutManager = LinearLayoutManager(
            requireContext() ,
            LinearLayoutManager.HORIZONTAL ,
            false
        )
        recommendedAdapter = RecommendedAdapter(requireContext() , emptyList())
        recommendedRecycler.adapter = recommendedAdapter
        recommendedRecycler.addItemDecoration(HorizontalItemDecoration(itemsToShow))
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recommendedRecycler)


        viewModel.recommendedArticles.observe(viewLifecycleOwner){
                articles ->
            val newsItems = articles?.map { article ->
                News(
                    image = article.urlToImage ,
                    title = article.title ,
                    description = article.description ,
                    date = article.publishedAt ,
                    source = article.url ,
                    author = article.author ,
                    category = "General",
                    content = article.content

                )
            }
            newsItems?.let {
                recommendedAdapter.updateNews(it)
            }
        }
        viewModel.fetchRecommendedNews()
        recommendedAdapter.onItemClick = {news ->
            val recentViewedNews = RecentViewedNews(
                id = news.title.hashCode().toString() ,
                title = news.title ,
                description = news.description,
                publishedAt = news.date ,
                author = news.author ,
                content = news.content ,
                category = "General" ,
                source = news.source,
                image = news.image ,
                timestamp = System.currentTimeMillis()

            )
            cacheViewModel.cacheNews(recentViewedNews)
            val intent = Intent(requireContext() , NewsActivity::class.java).apply {
                putExtra("news" , news)
            }
            startActivity(intent)
        }

        generalAdapter = GeneralAdapter( requireContext() , cacheViewModel.bookmarkedIds.value ?:emptySet()){
                news , shouldBookmark ->
            val bookmark = BookmarkedNews(
                id = news.title.hashCode().toString() ,
                title = news.title ,
                description = news.description ,
                publishedAt = news.date ,
                author = news.author,
                category = news.category,
                content = news.content ,
                image = news.image ,
                source = news.source,
                timestamp = System.currentTimeMillis()
            )
            if(shouldBookmark){
                Toast.makeText(requireContext(), "Bookmark Was Added" , Toast.LENGTH_SHORT).show()
                cacheViewModel.bookmark(bookmark)
            }
            else{
                Toast.makeText(requireContext(), "Bookmark Was Deleted" , Toast.LENGTH_SHORT).show()
                cacheViewModel.deleteBookmark(bookmark)
            }

        }
        cacheViewModel.bookmarkedIds.observe(viewLifecycleOwner){ids ->
            generalAdapter.updateBookmarks(ids )
        }
        val recyclerView : RecyclerView = binding.homeRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = generalAdapter
        recyclerView.isNestedScrollingEnabled = false

        viewModel.newArticles.observe(viewLifecycleOwner){
                articles ->
            val newsItems = articles?.map { article ->
                News(image = article.urlToImage ,
                    title = article.title ,
                    description = article.description ,
                    date = article.publishedAt ,
                    source = article.url,
                    author = article.author,
                    category = "General" ,
                    content = article.content
                )
            }
            if (newsItems != null) {
                generalAdapter.submitList(newsItems)
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner){error ->
            Toast.makeText(requireContext() , error , Toast.LENGTH_SHORT).show()
            Log.i("API" , error)

        }
        viewModel.getBreakingNews("us")


        generalAdapter.onItemClick = {news ->

            val recentViewedNews = RecentViewedNews(
                id = news.title.hashCode().toString(),
                title = news.title ,
                content = news.content ,
                publishedAt = news.date ,
                source = news.source ,
                description = news.description ,
                image = news.image ,
                category = "General",
                author = news.author ,
                timestamp = System.currentTimeMillis()
            )

            cacheViewModel.cacheNews(recentViewedNews)
            val intent = Intent(requireContext() , NewsActivity::class.java).apply {
                putExtra("news" , news)
            }
            startActivity(intent)
        }
    }


}