package za.org.grassroot.android.dagger;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import za.org.grassroot.android.services.RealmService;
import za.org.grassroot.android.services.RealmServiceImpl;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.services.UserDetailsServiceImpl;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationContext
    public Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    public AccountManager providesAccountManager(@ApplicationContext Context context) {
        return AccountManager.get(context);
    }

    @Provides
    @Singleton
    Realm provideRealm() {
        return Realm.getDefaultInstance();
    }

    @Provides
    @Singleton
    RealmService provideRealmService(final Realm realm) {
        return new RealmServiceImpl(realm);
    }

    @Provides
    @Singleton
    UserDetailsService provideUserDetailsService(AccountManager accountManager, RealmService realmService) {
        return new UserDetailsServiceImpl(accountManager, realmService);
    }

}
