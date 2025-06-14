package com.example.newsapp

import News
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.newsapp.databinding.ActivityNewsBinding
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val news: News? = intent.getParcelableExtra("news")
        if (news == null) {
            finish()
            return
        }

        binding.newsTitle.text = news.title
        binding.newsDescription.text = news.description ?: "No Description"
        binding.newsCategory.text = news.category

        try {
            val convertedDate = ZonedDateTime.parse(news.date)
            val format = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss z")
            binding.newsDate.text = convertedDate.format(format)
        } catch (e: Exception) {
            binding.newsDate.text = news.date // Fallback to original date if parsing fails
            Log.e("NewsActivity", "Error parsing date", e)
        }

        binding.authorName.text = news.author ?: "Author not found"
        binding.newsContent.text = news.content ?: "No Content"

        // Load image with Glide
        if (news.image != null) {
            Glide.with(this)
                .load(news.image)
                .placeholder(R.drawable.newspapers)
                .error(R.drawable.newspapers)
                .centerCrop()
                .into(binding.newsImage)
        } else {
            binding.newsImage.setImageResource(R.drawable.newspapers)
        }

        binding.sourceUrl.setOnClickListener {
            val url = news.source
            Log.i("API", url)
            if (url.startsWith("http")) {
                try {
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No browser found to open the URL", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}