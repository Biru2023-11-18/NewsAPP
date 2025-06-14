package com.example.newsapp.NewsFragment

import News
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.Adapter.GeneralAdapter
import com.example.newsapp.DataBaseforNews.BookmarkedNews
import com.example.newsapp.DataBaseforNews.RecentViewedNews
import com.example.newsapp.NewsActivity
import com.example.newsapp.ViewModels.recentViewedViewModel
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.ViewModels.NewsViewModelFactory
import com.example.newsapp.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding
    private var categoryName: String? = null
    private lateinit var viewModel: NewsViewModel
    private lateinit var generalAdapter: GeneralAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryName = arguments?.getString("category_name")
        binding = FragmentCategoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(categoryName: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString("category_name", categoryName)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emptyText = binding.emptyTextView


        viewModel = ViewModelProvider(this).get(NewsViewModel::class.java)

        val cacheLimit = 10
        val cacheViewModel = ViewModelProvider(
            this,
            NewsViewModelFactory(requireActivity().application, cacheLimit)
        ).get(recentViewedViewModel::class.java)

        generalAdapter = GeneralAdapter(requireContext(), cacheViewModel.bookmarkedIds.value ?: emptySet()) { news, shouldBookmark ->
            val bookmark = BookmarkedNews(
                id = news.title.hashCode().toString(),
                title = news.title,
                description = news.description,
                publishedAt = news.date,
                author = news.author,
                category = news.category,
                content = news.content,
                image = news.image,
                source = news.source,
                timestamp = System.currentTimeMillis()
            )
            if (shouldBookmark) {
                cacheViewModel.bookmark(bookmark)
                Toast.makeText(requireContext(), "Bookmark Was Added", Toast.LENGTH_SHORT).show()
            } else {
                cacheViewModel.deleteBookmark(bookmark)
                Toast.makeText(requireContext(), "Bookmark Was Deleted", Toast.LENGTH_SHORT).show()
            }
        }

        cacheViewModel.bookmarkedIds.observe(viewLifecycleOwner) { ids ->
            generalAdapter.updateBookmarks(ids)
        }

        val recyclerView: RecyclerView = binding.categoryRecyclerView
        generalAdapter.submitList(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = generalAdapter
        }


        generalAdapter.onItemClick = { news ->
            val recentViewedNews = RecentViewedNews(
                id = news.title.hashCode().toString(),
                title = news.title,
                content = news.content,
                publishedAt = news.date,
                source = news.source,
                description = news.description,
                image = news.image,
                category = categoryName.toString(),
                author = news.author,
                timestamp = System.currentTimeMillis()
            )
            cacheViewModel.cacheNews(recentViewedNews)

            val intent = Intent(requireContext(), NewsActivity::class.java).apply {
                putExtra("news", news)
            }
            startActivity(intent)
        }

        viewModel.newArticles.observe(viewLifecycleOwner) { articles ->
            val newsItem = articles?.map { article ->
                News(
                    image = article.urlToImage,
                    title = article.title,
                    description = article.description,
                    date = article.publishedAt,
                    source = article.url,
                    author = article.author,
                    category = categoryName.toString(),
                    content = article.content
                )
            }
            if (!newsItem.isNullOrEmpty()) {
                recyclerView.visibility = View.VISIBLE
                emptyText.visibility = View.GONE
                generalAdapter.submitList(newsItem)
            } else {
                recyclerView.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
                generalAdapter.submitList(emptyList())
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            Log.i("API", error)
        }

        viewModel.getCategoryNews("us", categoryName.toString())
    }
}