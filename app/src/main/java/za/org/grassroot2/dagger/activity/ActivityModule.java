package za.org.grassroot2.dagger.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.dagger.ActivityContext;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.dagger.user.UserScope;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.presenter.LoginPresenter;
import za.org.grassroot2.presenter.MainPresenter;
import za.org.grassroot2.presenter.fragment.SingleTextMultiButtonPresenter;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.LiveWireServiceImpl;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.MediaServiceImpl;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.rest.GrassrootAuthApi;
import za.org.grassroot2.util.StringDescriptionProvider;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class ActivityModule {

    private AppCompatActivity act;

    public ActivityModule(AppCompatActivity c) {
        act = c;
    }

    @Provides
    @ActivityContext
    AppCompatActivity activityContext(){
        return act;
    }

    @Provides
    @PerActivity
    RxPermissions providesRxPermission(@ActivityContext AppCompatActivity act) {
        return new RxPermissions(act);
    }

    @Provides
    @PerActivity
    MediaService provideMediaService(@ApplicationContext Context applicationContext, DatabaseService realmService,
                                     NetworkService networkService) {
        return new MediaServiceImpl(applicationContext, realmService, networkService);
    }

    @Provides
    @PerActivity
    MainPresenter provideMainPresenter(StringDescriptionProvider stringProvider,
                                       UserDetailsService userDetailsService,
                                       DatabaseService realmService,
                                       MediaService mediaService,
                                       LiveWireService liveWireService) {
        return new MainPresenter(stringProvider, userDetailsService, realmService, mediaService, liveWireService);
    }

    @Provides
    SingleTextMultiButtonPresenter provideSingleMultiButtonPresenter() {
        return new SingleTextMultiButtonPresenter();
    }

    @Provides
    @PerActivity
    LoginPresenter provideLoginPresenter(GrassrootAuthApi grassrootAuthApi,
                                         UserDetailsService userDetailsService) {
        return new LoginPresenter(grassrootAuthApi, userDetailsService);
    }

    @Provides
    @PerActivity
    LiveWireService provideLiveWireService(DatabaseService databaseService, NetworkService networkService) {
        return new LiveWireServiceImpl(databaseService, networkService);
    }

}
