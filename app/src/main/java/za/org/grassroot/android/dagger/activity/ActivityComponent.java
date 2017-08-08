package za.org.grassroot.android.dagger.activity;

import dagger.Subcomponent;
import za.org.grassroot.android.view.LoginActivity;
import za.org.grassroot.android.view.activity.GrassrootActivity;

/**
 * Created by luke on 2017/08/08.
 */
@PerActivity
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(GrassrootActivity grassrootActivity);

}
