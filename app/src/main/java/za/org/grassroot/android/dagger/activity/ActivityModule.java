package za.org.grassroot.android.dagger.activity;

import android.accounts.AccountManager;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.services.rest.GrassrootAuthApi;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class ActivityModule {

    @Provides
    @PerActivity
    LoginPresenter provideLoginPresenter(GrassrootAuthApi grassrootAuthApi, AccountManager accountManager) {
        Timber.v("called provide login presenter");
        return new LoginPresenter(grassrootAuthApi, accountManager); // for now, switch to implementation pattern soon
    }

}
