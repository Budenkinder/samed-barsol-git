package com.budenkinder.shisha.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.budenkinder.shisha.data.Item
import com.budenkinder.shisha.data.ShishaData

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(
    private val dao: ShishaData?,
    fm: FragmentManager
) :
    FragmentPagerAdapter(fm) {

    private val tabTitles = ArrayList<String?>()

    init {
        for (i in 0 until dao?.tabs?.size!!) {
            val tabList = dao.tabs!![i] as List<Item>
            tabTitles.add(tabList[0].tab_title)
        }
    }

    override fun getItem(position: Int): Fragment {
        return MenuFragment.newInstance(dao, position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return dao?.tabs?.size!!
    }
}