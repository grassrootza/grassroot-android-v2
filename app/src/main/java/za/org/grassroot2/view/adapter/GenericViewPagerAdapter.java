package za.org.grassroot2.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GenericViewPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> adapterFragmentList = new ArrayList<>();
    private final List<String>   adapterTitleList    = new ArrayList<>();

    public GenericViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return adapterFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return adapterFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        adapterFragmentList.add(fragment);
        adapterTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return adapterTitleList.get(position);
    }


}
