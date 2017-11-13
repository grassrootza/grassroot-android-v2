package za.org.grassroot2.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import retrofit2.http.Url;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.services.LocationManager;
import za.org.grassroot2.services.SyncOfflineDataService;
import za.org.grassroot2.util.NetworkUtil;
import za.org.grassroot2.view.fragment.AroundMeFragment;
import za.org.grassroot2.view.fragment.GroupsFragment;
import za.org.grassroot2.view.fragment.HomeFragment;
import za.org.grassroot2.view.fragment.MeFragment;

public class DashboardActivity extends GrassrootActivity {

    public static final int TAB_HOME   = 0;
    public static final int TAB_GROUPS = 1;
    public static final int TAB_AROUND = 2;
    public static final int TAB_ME     = 3;
    @BindView(R.id.contentPager) ViewPager            pager;
    @BindView(R.id.navigation)   BottomNavigationView bottomNavigation;
    @Inject                      RxPermissions        rxPermissions;
    @Inject                      LocationManager      manager;

    private MenuItem menuItem;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                pager.setCurrentItem(TAB_HOME);
                return true;
            case R.id.navigation_groups:
                pager.setCurrentItem(TAB_GROUPS);
                return true;
            case R.id.navigation_me:
                pager.setCurrentItem(TAB_ME);
                return true;
            case R.id.navigation_around:
                pager.setCurrentItem(TAB_AROUND);
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pager.setAdapter(new DashboardFragmentAdapter(getSupportFragmentManager()));
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigation.getMenu().getItem(TAB_HOME).setChecked(false);
                }
                bottomNavigation.getMenu().getItem(position).setChecked(true);
                menuItem = bottomNavigation.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (NetworkUtil.hasInternetAccess(this)) {
            startService(new Intent(this, SyncOfflineDataService.class));
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dashboard;
    }

    @Override
    protected void onInject(ActivityComponent component) {
        component.inject(this);
    }

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, DashboardActivity.class));
    }

    private class DashboardFragmentAdapter extends FragmentPagerAdapter {

        DashboardFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_HOME:
                    return new HomeFragment();
                case TAB_GROUPS:
                    return GroupsFragment.Companion.newInstance();
                case TAB_AROUND:
                    return AroundMeFragment.Companion.newInstance();
                case TAB_ME:
                    return new MeFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.disconnect();
    }
}
