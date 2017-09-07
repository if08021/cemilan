package com.example.myfblogin

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.myfblogin.adapter.ListPagerAdapter
import com.example.myfblogin.component.BottomNavigationViewHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private var currentTabPosition = 0

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                Log.d("AAA", "onclick bottom tab home")
                item.icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.tab_icon_home)
                item.isChecked = true
                navigation.refreshDrawableState()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_explore -> {
                Log.d("AAA", "onclick bottom tab explore")
                item.icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.tab_icon_explore)
                item.isChecked = true
                navigation.refreshDrawableState()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_activity -> {
                Log.d("AAA", "onclick bottom tab activity")
                item.icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.tab_icon_activity)
                item.isChecked = true
                navigation.invalidate()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                Log.d("AAA", "onclick bottom tab profile")
                item.icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.tab_icon_profile)
                item.isChecked = true
                navigation.refreshDrawableState()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myAdapter = ListPagerAdapter(this, supportFragmentManager)
        pager.adapter = myAdapter
        pager.offscreenPageLimit = 1
        mTabs!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                mTabs!!.updateTabStyles()
                currentTabPosition = position
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

            override fun onPageScrollStateChanged(arg0: Int) {}
        })
        mTabs!!.setViewPager(pager)
        pager.currentItem = 0

        app_bar_layout.addView(onCreateTab(mTabs!!), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.header_tab_height)))

        //disable auto-hide header in this activity
        val layoutParams = toolbar.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = 0

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        BottomNavigationViewHelper.disableShiftMode(navigation)
        navigation.selectedItemId = R.id.navigation_home

        val signInIntent = Intent(this, PagerListActivity::class.java)
        startActivity(signInIntent)

        hideHeader()
    }

    override fun onCreateHeaderAction() {}
}
