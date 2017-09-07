package com.example.myfblogin.adapter

import ListItemFragment
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.example.myfblogin.R
import com.example.myfblogin.component.PagerSlidingTabStrip

class ListPagerAdapter(context: Context, fragmentManager: FragmentManager) : BasePagerAdapter(context, fragmentManager, null), PagerSlidingTabStrip.TextTabProvider {

    override fun getItem(position: Int): Fragment {
        if (mPageReferenceMap!!.get(position) == null) {
            val f = ListItemFragment.newInstance(position)
            mPageReferenceMap!!.put(position, f)
        }
        return mPageReferenceMap!!.get(position)
    }

    override fun getCount(): Int {
        return getTitles().size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return getTitles()[position]
    }

    private fun getTitles(): Array<String> {
        return mContext!!.resources.getStringArray(R.array.article_list_tabs)
    }
}
