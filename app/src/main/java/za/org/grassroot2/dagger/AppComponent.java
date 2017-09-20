package za.org.grassroot2.dagger;

import javax.inject.Singleton;

import dagger.Component;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.activity.ActivityModule;
import za.org.grassroot2.dagger.user.ApiModule;
import za.org.grassroot2.services.account.GrassrootAuthService;
import za.org.grassroot2.services.account.SyncAdapter;

/**
 * Created by luke on 2017/08/08.
 */
@Singleton
@Component(modules = {AppModule.class, NetworkModule.class, ApiModule.class, NoAuthApiModule.class})
public interface AppComponent {

    void inject(SyncAdapter syncAdapter);
    void inject(GrassrootAuthService service);

    // void inject(LoggedInViewPresenterImpl target);
    ActivityComponent plus(ActivityModule activityModule);
}