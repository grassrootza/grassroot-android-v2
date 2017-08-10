package za.org.grassroot.android.dagger.activity;

import android.accounts.AccountManager;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.presenter.MainPresenter;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.services.rest.GrassrootAuthApi;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class ActivityModule {

    @Provides
    @PerActivity
    LoginPresenter provideLoginPresenter(GrassrootAuthApi grassrootAuthApi,
                                         AccountManager accountManager,
                                         UserDetailsService userDetailsService) {
        return new LoginPresenter(grassrootAuthApi, userDetailsService);
    }

    @Provides
    @PerActivity
    MainPresenter provideMainPresenter(UserDetailsService userDetailsService) {
        return new MainPresenter(userDetailsService);
    }

}
