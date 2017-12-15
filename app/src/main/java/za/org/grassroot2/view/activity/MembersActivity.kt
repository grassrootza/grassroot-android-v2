package za.org.grassroot2.view.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_group_members.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.view.dialog.GenericMessageDialog
import za.org.grassroot2.view.fragment.MemberListFragment
import za.org.grassroot2.view.fragment.MemberLogsFragment

/**
 * Created by luke on 2017/12/10.
 */
class MembersActivity : GrassrootActivity() {

    private var groupUid: String? = null
    var menuItem: MenuItem? = null

    override fun getLayoutResourceId(): Int = R.layout.activity_group_members

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        groupUid = intent.getStringExtra(GROUP_UID_FIELD)

        contentPager.adapter = MembersDashboardFragmentAdapter(supportFragmentManager)
        navigation.enableAnimation(false)
        navigation.enableShiftingMode(false)
        navigation.enableItemShiftingMode(false)

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.members_list -> {
                    contentPager.currentItem = TAB_LIST
                    true
                }
                R.id.members_history -> {
                    contentPager.currentItem = TAB_HISTORY
                    true
                }
                R.id.members_permissions -> {
                    contentPager.currentItem = TAB_PERMISSIONS
                    true
                }
                else -> false
            }
        }

        contentPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
            override fun onPageScrollStateChanged(state: Int) { }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) menuItem?.setChecked(false) else navigation.menu.getItem(TAB_LIST).setChecked(false)
                navigation.menu.getItem(position).setChecked(true)
                menuItem = navigation.menu.getItem(position)
            }
        })
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    inner class MembersDashboardFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                TAB_LIST -> MemberListFragment.newInstance(groupUid!!)
                TAB_HISTORY -> MemberLogsFragment.newInstance(groupUid!!)
                TAB_PERMISSIONS -> MemberListFragment.newInstance(groupUid!!)
                else -> GenericMessageDialog.newInstance("Huh")
            }
        }

        override fun getCount(): Int = 2
    }

    companion object {
        val GROUP_UID_FIELD = "group_uid"

        private val TAB_LIST = 0;
        private val TAB_HISTORY = 1;
        private val TAB_PERMISSIONS = 2;
    }



}