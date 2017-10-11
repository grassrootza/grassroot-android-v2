package za.org.grassroot2.view.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.org.grassroot2.R;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.view.fragment.GroupsFragment;

public class DashboardActivity extends GrassrootActivity {

    @BindView(R.id.navigation) BottomNavigationView bottomNavigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
            case R.id.navigation_groups:
                addFragment(GroupsFragment.newInstance(), GroupsFragment.class);
                return true;
            case R.id.navigation_me:
                return true;
        }
        return false;
    };

    private boolean fragmentIsAttached(Class fragment) {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null && !fragmentList.isEmpty()) {
            for (Fragment attachedFragment : fragmentList) {
                if (attachedFragment != null && attachedFragment.getClass().equals(fragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addFragment(Fragment fragment, Class cls) {
        if (!fragmentIsAttached(cls)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigation.setSelectedItemId(R.id.navigation_groups);
    }

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, DashboardActivity.class));
    }
}
