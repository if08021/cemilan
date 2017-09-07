package com.example.myfblogin

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.myfblogin.adapter.ListPagerAdapter
import com.example.myfblogin.component.BottomNavigationViewHelper
import com.example.myfblogin.component.PagerSlidingTabStrip
import kotlinx.android.synthetic.main.activity_pager_list.*

class PagerListActivity : BaseActivity() {

    private var currentTabPosition = 0

    override fun onCreateHeaderAction() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager_list)
        setLeftMenuIcon(R.drawable.ic_back, true)

        title = "Tab Page"

        val myAdapter = ListPagerAdapter(this, supportFragmentManager)
        pager.adapter = myAdapter
        pager.offscreenPageLimit = 1
        mTabs!!.setTabIndicatorType(PagerSlidingTabStrip.INDICATOR_MATCH_PARENT)
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

        app_bar_layout.addView(onCreateTab(mTabs!!), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        //disable auto-hide header in this activity
        val layoutParams = toolbar.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = 0
    }
}
