package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class SecondActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second);

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigation.selectedItemId = R.id.item_1

        val adapter = MyAdapter(supportFragmentManager)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(0)

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageScrollStateChanged(i: Int) {}
            override fun onPageSelected(i: Int) {
                when(i){
                    0 -> bottomNavigation.selectedItemId = R.id.item_1
                    1 -> bottomNavigation.selectedItemId = R.id.item_2
                }
            }
        })

        bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> viewPager.setCurrentItem(0)
                R.id.item_2 -> viewPager.setCurrentItem(1)
                else -> viewPager.setCurrentItem(0)
            }
            true
        })

    }


    class MyAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> MainPerson()
                1 -> Profile()
                else -> MainPerson()
            }
        }
    }

}