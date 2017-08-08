package za.org.grassroot.android.dagger;

import javax.inject.Singleton;

import dagger.Component;
import za.org.grassroot.android.dagger.login.LoginSignUpComponent;
import za.org.grassroot.android.dagger.login.NoAuthApiModule;
import za.org.grassroot.android.dagger.user.ApiModule;
import za.org.grassroot.android.dagger.user.AuthModule;
import za.org.grassroot.android.dagger.user.UserComponent;
import za.org.grassroot.android.view.activity.GrassrootActivity;

/**
 * Created by luke on 2017/08/08.
 */
@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(GrassrootActivity target);
    LoginSignUpComponent plus(NoAuthApiModule noAuthApiModule);
    UserComponent plus(AuthModule authModule, ApiModule apiModule);

}