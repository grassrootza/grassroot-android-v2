package za.org.grassroot2.dagger.user;

import dagger.Subcomponent;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.activity.ActivityModule;
import za.org.grassroot2.services.account.SyncAdapter;

/**
 * Created by luke on 2017/08/08.
 */
@UserScope
@Subcomponent(modules = {ApiModule.class, UserPresenterModule.class})
public interface UserComponent {

    void inject(SyncAdapter target);
    ActivityComponent plus(ActivityModule activityModule);

}
