package za.org.grassroot2.dagger.activity;

import dagger.Subcomponent;
import za.org.grassroot2.view.activity.MainActivity;

/**
 * Created by luke on 2017/08/08.
 */
@PerActivity
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(MainActivity target);

}