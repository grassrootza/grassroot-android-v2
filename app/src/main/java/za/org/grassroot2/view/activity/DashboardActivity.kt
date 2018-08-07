package za.org.grassroot2.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.MenuItem
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_dashboard.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.services.LocationManager
import za.org.grassroot2.services.SyncOfflineDataService
import za.org.grassroot2.util.NetworkUtil
import za.org.grassroot2.view.fragment.AroundMeFragment
import za.org.grassroot2.view.fragment.GroupsFragment
import za.org.grassroot2.view.fragment.HomeFragment
import za.org.grassroot2.view.fragment.MeFragment
import javax.inject.Inject

class DashboardActivity : GrassrootActivity() {
    override val layoutResourceId: Int
        get() = R.layout.activity_dashboard

    @Inject internal lateinit var rxPermissions: RxPermissions
    @Inject internal lateinit var manager: LocationManager

    private var menuItem: MenuItem? = null

    private val mOnNavigationItemSelectedListener:(MenuItem) -> Boolean = { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                contentPager.currentItem = TAB_HOME
                true
            }
            R.id.navigation_groups -> {
                contentPager.currentItem = TAB_GROUPS
                true
            }
            R.id.navigation_me -> {
                contentPager.currentItem = TAB_ME
                true
            }
            R.id.navigation_around -> {
                contentPager.currentItem = TAB_AROUND
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentPager.adapter = DashboardFragmentAdapter(supportFragmentManager)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.enableAnimation(false)
        navigation.enableShiftingMode(false)
        navigation.enableItemShiftingMode(false)
        contentPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) {
                    menuItem!!.isChecked = false
                } else {
                    navigation.menu.getItem(TAB_HOME).isChecked = false
                }
                navigation.menu.getItem(position).isChecked = true
                menuItem = navigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        if (NetworkUtil.hasInternetAccess(this)) {
            startService(Intent(this, SyncOfflineDataService::class.java))
        }

        val notificationText = intent.getStringExtra("notificationText")
        if (notificationText != null) {
            showMessageDialog(notificationText)
        }

    }

    override fun onInject(component: ActivityComponent) = component.inject(this)

    private inner class DashboardFragmentAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            when (position) {
                TAB_HOME -> return HomeFragment()
                TAB_GROUPS -> return GroupsFragment.newInstance()
                TAB_AROUND -> return AroundMeFragment.newInstance()
                TAB_ME -> return MeFragment.newInstance()
            }
            return null
        }

        override fun getCount(): Int {
            return 4
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.disconnect()
    }

    companion object {

        const val TAB_HOME = 0
        const val TAB_GROUPS = 1
        const val TAB_AROUND = 2
        const val TAB_ME = 3

        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DashboardActivity::class.java))
        }
    }
}
