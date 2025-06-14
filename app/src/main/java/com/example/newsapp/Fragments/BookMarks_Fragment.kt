package com.example.newsapp.Fragments



import News
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.Adapter.GeneralAdapter
import com.example.newsapp.DataBaseforNews.BookmarkedNews
import com.example.newsapp.DataBaseforNews.RecentViewedNews
import com.example.newsapp.NewsActivity
import com.example.newsapp.ViewModels.NewsViewModelFactory
import com.example.newsapp.ViewModels.recentViewedViewModel
import com.example.newsapp.databinding.FragmentBookMarksBinding

class BookMarks_Fragment : Fragment() {
    private lateinit var binding: FragmentBookMarksBinding
    private lateinit var cacheViewModel: recentViewedViewModel
    private lateinit var generalAdapter: GeneralAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookMarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeBookmarks()
    }

    private fun setupViewModel() {
        val cacheLimit = 10
        cacheViewModel = ViewModelProvider(
            this,
            NewsViewModelFactory(requireActivity().application, cacheLimit)
        ).get(recentViewedViewModel::class.java)
    }

    private fun setupRecyclerView() {
        generalAdapter = GeneralAdapter(
            requireContext(),
            cacheViewModel.bookmarkedIds.value ?: emptySet()
        ) { news, shouldBookmark ->
            handleBookmarkAction(news, shouldBookmark)
        }

        cacheViewModel.bookmarkedIds.observe(viewLifecycleOwner) { ids ->
            generalAdapter.updateBookmarks(ids)
        }

        binding.bookmarksRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = generalAdapter
        }

        generalAdapter.onItemClick = { news ->
            handleNewsItemClick(news)
        }
    }

    private fun handleBookmarkAction(news: News, shouldBookmark: Boolean) {
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

    private fun handleNewsItemClick(news: News) {
        val recentViewedNews = RecentViewedNews(
            id = news.title.hashCode().toString(),
            title = news.title,
            content = news.content,
            publishedAt = news.date,
            source = news.source,
            description = news.description,
            image = news.image,
            category = news.category ?: "bookmarks",
            author = news.author,
            timestamp = System.currentTimeMillis()
        )
        cacheViewModel.cacheNews(recentViewedNews)

        val intent = Intent(requireContext(), NewsActivity::class.java).apply {
            putExtra("news", news)
        }
        startActivity(intent)
    }

    private fun observeBookmarks() {
        cacheViewModel.bookmarkNews.observe(viewLifecycleOwner) { bookmarks ->
            val newItem = bookmarks?.map { bookmark ->
                News(
                    image = bookmark.image,
                    title = bookmark.title,
                    description = bookmark.description,
                    content = bookmark.content,
                    category = bookmark.category,
                    source = bookmark.source,
                    author = bookmark.author,
                    date = bookmark.publishedAt
                )
            }

            updateUI(newItem)
        }
    }

    private fun updateUI(newsList: List<News>?) {
        if (!newsList.isNullOrEmpty()) {
            binding.bookmarksRecycler.visibility = View.VISIBLE
            binding.emptyStateText.visibility = View.GONE
            generalAdapter.submitList(newsList)
        } else {
            binding.bookmarksRecycler.visibility = View.GONE
            binding.emptyStateText.visibility = View.VISIBLE
            generalAdapter.submitList(emptyList())
        }
    }
}