package com.example.newsapp.Adapter

import News
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GeneralAdapter(
    private val context: Context,
    private var bookmarkIds: Set<String> = emptySet(),
     var onBookmarkClick: (News, Boolean) -> Unit
) : RecyclerView.Adapter<GeneralAdapter.ViewHolder>() {

    private val newsList = mutableListOf<News>()
    var onItemClick: ((News) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.news_card_title)
        val description: TextView = view.findViewById(R.id.news_card_description)
        val category: TextView = view.findViewById(R.id.news_card_category)
        val date: TextView = view.findViewById(R.id.news_card_date)
        val image: ImageView = view.findViewById(R.id.news_card_image)
        val bookmarkIcon: ImageView = view.findViewById(R.id.bookmark)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<News>) {
        newsList.clear()
        newsList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = newsList.size

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = newsList[position]

        holder.title.text = item.title
        holder.description.text = item.description
        holder.category.text = item.category

        val convertedDate = ZonedDateTime.parse(item.date)
        val format = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss z")
        holder.date.text = convertedDate.format(format)

        item.image?.let { url ->
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.newspapers)
                .error(R.drawable.newspapers)
                .centerCrop()
                .into(holder.image)
        } ?: holder.image.setImageResource(R.drawable.newspapers)
       val uniqueKey = getUniqueKey(item)
        val isBookmarked = bookmarkIds.contains(uniqueKey)
        updateBookmarkIcon(holder, isBookmarked)

        holder.bookmarkIcon.setOnClickListener {
            val newBookmarkState = !isBookmarked

            updateBookmarkIcon(holder, newBookmarkState)

            updateLocalBookmarkState(uniqueKey, newBookmarkState)

            onBookmarkClick(item, newBookmarkState)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    private fun updateBookmarkIcon(holder: ViewHolder, isBookmarked: Boolean) {
        holder.bookmarkIcon.setImageResource(
            if (isBookmarked) R.drawable.baseline_turned_in_24
            else R.drawable.baseline_turned_in_not_24
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBookmarks(newBookmarkIds: Set<String>) {
        bookmarkIds = newBookmarkIds
        notifyDataSetChanged()
    }

    private fun updateLocalBookmarkState(uniqueKey: String, shouldBookmark: Boolean) {
        val updated = bookmarkIds.toMutableSet().apply {
            if (shouldBookmark) add(uniqueKey) else remove(uniqueKey)
        }
        bookmarkIds = updated
    }

    private fun getUniqueKey(news: News): String {

        return news.title.hashCode().toString()
    }
}