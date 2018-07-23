package za.org.grassroot2.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

import java.util.ArrayList

class GenericViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {

    private val adapterFragmentList = ArrayList<Fragment>()
    private val adapterTitleList = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return adapterFragmentList[position]
    }

    override fun getCount(): Int {
        return adapterFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        adapterFragmentList.add(fragment)
        adapterTitleList.add(title)
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return adapterTitleList[position]
    }

    override fun getItemPosition(`object`: Any): Int {
        val index = adapterFragmentList.indexOf(`object`)
        return if (index == -1) PagerAdapter.POSITION_NONE else index
    }

    fun removeLast() {
        adapterFragmentList.removeAt(adapterFragmentList.size - 1)
        adapterTitleList.removeAt(adapterTitleList.size - 1)
        notifyDataSetChanged()
    }

    fun removeAllAbove(currentPosition: Int) {
        while (currentPosition + 1 < adapterFragmentList.size) {
            adapterFragmentList.removeAt(adapterFragmentList.size - 1)
            adapterTitleList.removeAt(adapterTitleList.size - 1)
        }
        notifyDataSetChanged()
    }
}
