package com.example.newsapp.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsapp.NewsFragment.CategoryFragment
import com.example.newsapp.NewsFragment.HomeFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity ) :
    FragmentStateAdapter(fragmentActivity){

        private val categoryName = listOf(
            "general" , "business" , "entertainment" ,
            "health" , "science" , "sports" , "technology"
        )
    override fun getItemCount(): Int {
        return categoryName.size
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            else -> CategoryFragment.newInstance(categoryName[position])
        }

    }

}