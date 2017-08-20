package za.org.grassroot.android.dagger.activity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot.android.dagger.ApplicationContext;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.presenter.MainPresenter;
import za.org.grassroot.android.presenter.MainPresenterImpl;
import za.org.grassroot.android.services.LiveWireService;
import za.org.grassroot.android.services.MediaService;
import za.org.grassroot.android.services.RealmService;
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
                                         UserDetailsService userDetailsService) {
        return new LoginPresenter(grassrootAuthApi, userDetailsService);
    }

    @Provides
    @PerActivity
    MainPresenter provideMainPresenter(@ApplicationContext Context applicationContext,
                                       UserDetailsService userDetailsService,
                                       RealmService realmService,
                                       MediaService mediaService,
                                       LiveWireService liveWireService) {
        return new MainPresenterImpl(applicationContext, userDetailsService, realmService, mediaService, liveWireService);
    }

}
