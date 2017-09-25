package za.org.grassroot2.dagger.user;

import dagger.Subcomponent;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.activity.ActivityModule;
import za.org.grassroot2.services.account.GrassrootAuthService;
import za.org.grassroot2.services.account.SyncAdapter;
import za.org.grassroot2.view.activity.MainActivity;

/**
 * Created by luke on 2017/08/08.
 */
@UserScope
@Subcomponent(modules = {UserPresenterModule.class, ApiModule.class})
public interface UserComponent {

}
