package za.org.grassroot.android.dagger.activity;

import dagger.Subcomponent;
import za.org.grassroot.android.view.activity.MainActivity;

/**
 * Created by luke on 2017/08/08.
 */
@PerActivity
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(MainActivity target);

}