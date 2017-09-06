package za.org.grassroot2.dagger.user;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.LiveWireServiceImpl;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.MediaServiceImpl;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.RealmService;

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