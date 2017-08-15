package za.org.grassroot.android.dagger;

import javax.inject.Singleton;

import dagger.Component;
import za.org.grassroot.android.dagger.login.LoginSignUpComponent;
import za.org.grassroot.android.dagger.login.NoAuthApiModule;
import za.org.grassroot.android.dagger.user.ApiModule;
import za.org.grassroot.android.dagger.user.UserComponent;
import za.org.grassroot.android.presenter.LoggedInViewPresenterImpl;
import za.org.grassroot.android.view.activity.GrassrootActivity;

/**
 * Created by luke on 2017/08/08.
 */
@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(GrassrootActivity target);
    void inject(LoggedInViewPresenterImpl target);

    LoginSignUpComponent plus(NoAuthApiModule noAuthApiModule);
    UserComponent plus(ApiModule apiModule);

}