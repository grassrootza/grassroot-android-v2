package za.org.grassroot2.dagger.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.dagger.ActivityContext;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.presenter.ForgottenPasswordPresenter;
import za.org.grassroot2.presenter.RegistrationPresenter;
import za.org.grassroot2.presenter.activity.GroupDetailsPresenter;
import za.org.grassroot2.presenter.activity.GroupSettingsPresenter;
import za.org.grassroot2.presenter.activity.PickContactPresenter;
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter;
import za.org.grassroot2.presenter.fragment.SingleTextMultiButtonPresenter;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.LiveWireServiceImpl;
import za.org.grassroot2.services.LocationManager;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.MediaServiceImpl;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.rest.GrassrootAuthApi;
import za.org.grassroot2.util.ContactHelper;
import za.org.grassroot2.util.ImageUtil;
import za.org.grassroot2.util.MediaRecorderWrapper;
import za.org.grassroot2.view.adapter.HomeAdapter;
import za.org.grassroot2.view.adapter.PostAdapter;

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
    AppCompatActivity activityContext() {
        return act;
    }

    @Provides
    @PerActivity
    RxPermissions providesRxPermission(@ActivityContext AppCompatActivity act) {
        return new RxPermissions(act);
    }

    @Provides
    @PerActivity
    ImageUtil provideImageUtil(@ActivityContext AppCompatActivity act) {
        return new ImageUtil(act);
    }

    @Provides
    @PerActivity
    MediaService provideMediaService(@ApplicationContext Context applicationContext, DatabaseService realmService,
                                     ImageUtil imageUtil) {
        return new MediaServiceImpl(applicationContext, realmService, imageUtil);
    }

    @Provides
    SingleTextMultiButtonPresenter provideSingleMultiButtonPresenter() {
        return new SingleTextMultiButtonPresenter();
    }

    @Provides
    @PerActivity
    GroupFragmentPresenter provideGroupFragmentPresenter(DatabaseService dbService, UserDetailsService networkService) {
        return new GroupFragmentPresenter(dbService, networkService);
    }

    @Provides
    @PerActivity
    GroupDetailsPresenter provideGroupDetailsPresenter(DatabaseService dbService, NetworkService networkService) {
        return new GroupDetailsPresenter(dbService, networkService);
    }

    @Provides
    @PerActivity
    PickContactPresenter providePickContactPresenter(ContactHelper helper) {
        return new PickContactPresenter(helper);
    }

    @Provides
    @PerActivity
    GroupSettingsPresenter provideGroupSettingsPresenter(DatabaseService dbService, NetworkService networkService) {
        return new GroupSettingsPresenter(networkService, dbService);
    }

    @Provides
    @PerActivity
    LiveWireService provideLiveWireService(DatabaseService databaseService, NetworkService networkService) {
        return new LiveWireServiceImpl(databaseService, networkService);
    }

    @Provides
    @PerActivity
    RegistrationPresenter provideRegistrationPresenter(GrassrootAuthApi grassrootAuthApi,
                                                       UserDetailsService userDetailsService) {
        return new RegistrationPresenter(grassrootAuthApi, userDetailsService);
    }

    @Provides
    @PerActivity
    HomeAdapter provideHomeAdapter(@ActivityContext AppCompatActivity c) {
        return new HomeAdapter(c, new ArrayList<>());
    }

    @Provides
    @PerActivity
    PostAdapter providePostAdapter(@ActivityContext AppCompatActivity c) {
        return new PostAdapter(c, new ArrayList<>());
    }

    @Provides
    @PerActivity
    ForgottenPasswordPresenter provideForgottenPasswordPresenter(GrassrootAuthApi grassrootAuthApi,
                                                                 UserDetailsService userDetailsService) {
        return new ForgottenPasswordPresenter(grassrootAuthApi, userDetailsService);
    }

    @Provides
    @PerActivity
    LocationManager providesLocationManager(@ActivityContext AppCompatActivity act) {
        return new LocationManager(act);
    }

    @Provides
    @PerActivity
    MediaRecorderWrapper providesMediaRecorderWrapper(@ActivityContext AppCompatActivity act) {
        return new MediaRecorderWrapper(act);
    }

}
