package za.org.grassroot2.dagger;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.database.DatabaseHelper;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.database.DatabaseServiceImpl;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.UserDetailsServiceImpl;

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
    DatabaseService provideDatabase(DatabaseHelper helper) {
        return new DatabaseServiceImpl(helper);
    }

    @Provides
    @Singleton
    DatabaseHelper provideDatabaseHelper(@ApplicationContext  Context context) {
        return new DatabaseHelper(context);
    }

    @Provides
    @Singleton
    UserDetailsService provideUserDetailsService(AccountManager accountManager, DatabaseService databaseService) {
        return new UserDetailsServiceImpl(accountManager, databaseService);
    }

}
