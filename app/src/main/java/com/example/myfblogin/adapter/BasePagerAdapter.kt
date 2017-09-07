package com.example.myfblogin.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.SparseArray
import com.example.myfblogin.component.PagerSlidingTabStrip

abstract class BasePagerAdapter(context: Context, fragmentManager: FragmentManager, titles: Array<String>?) : FragmentStatePagerAdapter(fragmentManager), PagerSlidingTabStrip.TextTabProvider {
    var mContext: Context? = null
    var mFragmentManager: FragmentManager? = null
    var mTitles: Array<String>? = null
    var mPageReferenceMap: SparseArray<Fragment>? = null

    init {
        mFragmentManager = fragmentManager
        mContext = context
        mPageReferenceMap = SparseArray<Fragment>()
        mTitles = titles
    }

    fun updateTitles(titles: Array<String>) {
        this.mTitles = titles
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mTitles!![position]
    }

    override fun getCount(): Int {
        return mTitles!!.size
    }

    fun getFragment(pos: Int): Fragment {
        return mPageReferenceMap!!.get(pos)
    }

    override fun getItemPosition(`object`: Any?): Int {
        return POSITION_NONE
    }

    override fun getTabType(position: Int): Byte {
        return PagerSlidingTabStrip.TEXT_DECORATION_NONE
    }
}
