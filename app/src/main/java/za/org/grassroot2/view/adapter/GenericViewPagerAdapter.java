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
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return adapterTitleList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        int index = adapterFragmentList.indexOf(object);
        return index == -1 ?  POSITION_NONE :index;
    }

    public void removeLast() {
        adapterFragmentList.remove(adapterFragmentList.size()-1);
        adapterTitleList.remove(adapterTitleList.size()-1);
        notifyDataSetChanged();
    }

    public void removeAllAbove(int currentPosition) {
        while (currentPosition+1 < adapterFragmentList.size()) {
            adapterFragmentList.remove(adapterFragmentList.size()-1);
            adapterTitleList.remove(adapterTitleList.size()-1);
        }
        notifyDataSetChanged();
    }
}
