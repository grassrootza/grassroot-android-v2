package za.org.grassroot2.dagger;

import javax.inject.Singleton;

import dagger.Component;
import za.org.grassroot2.dagger.login.LoginSignUpComponent;
import za.org.grassroot2.dagger.login.NoAuthApiModule;
import za.org.grassroot2.dagger.user.ApiModule;
import za.org.grassroot2.dagger.user.UserComponent;
import za.org.grassroot2.view.activity.GrassrootActivity;

/**
 * Created by luke on 2017/08/08.
 */
@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(GrassrootActivity target);
    // void inject(LoggedInViewPresenterImpl target);

    LoginSignUpComponent plus(NoAuthApiModule noAuthApiModule);
    UserComponent plus(ApiModule apiModule);

}