package za.org.grassroot2.dagger.activity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.presenter.LoginPresenter;
import za.org.grassroot2.presenter.MainPresenter;
import za.org.grassroot2.presenter.MainPresenterImpl;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.rest.GrassrootAuthApi;

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
                                       DatabaseService realmService,
                                       MediaService mediaService,
                                       LiveWireService liveWireService) {
        return new MainPresenterImpl(applicationContext, userDetailsService, realmService, mediaService, liveWireService);
    }

}
