package za.org.grassroot2.view.fragment;

import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.fragment.FragmentComponent;

/**
 * Created by qbasso on 01.11.2017.
 */

public class MeFragment extends GrassrootFragment{
    @Override
    protected void onInject(ActivityComponent activityComponent) {

    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_me;
    }
}
