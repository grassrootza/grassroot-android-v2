package za.org.grassroot.android.dagger.user;

import dagger.Subcomponent;
import za.org.grassroot.android.dagger.activity.ActivityComponent;
import za.org.grassroot.android.dagger.activity.ActivityModule;

/**
 * Created by luke on 2017/08/08.
 */
@UserScope
@Subcomponent(modules = {ApiModule.class, UserPresenterModule.class})
public interface UserComponent {

    ActivityComponent plus(ActivityModule activityModule);

}
