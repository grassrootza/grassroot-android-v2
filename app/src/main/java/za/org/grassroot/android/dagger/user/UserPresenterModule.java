package za.org.grassroot.android.dagger.user;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot.android.dagger.ApplicationContext;
import za.org.grassroot.android.services.LiveWireService;
import za.org.grassroot.android.services.LiveWireServiceImpl;
import za.org.grassroot.android.services.MediaService;
import za.org.grassroot.android.services.MediaServiceImpl;
import za.org.grassroot.android.services.NetworkService;
import za.org.grassroot.android.services.RealmService;

/**
 * Created by luke on 2017/08/11.
 */
@Module
public class UserPresenterModule {

    @Provides
    @UserScope
    MediaService provideMediaService(@ApplicationContext Context applicationContext, RealmService realmService,
                                     NetworkService networkService) {
        return new MediaServiceImpl(applicationContext, realmService, networkService);
    }

    @Provides
    @UserScope
    LiveWireService provideLiveWireService(RealmService realmService, NetworkService networkService) {
        return new LiveWireServiceImpl(realmService, networkService);
    }

}