package za.org.grassroot.android.dagger.user;

import android.accounts.AccountManager;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot.android.services.auth.GrassrootAuthService;
import za.org.grassroot.android.services.auth.GrassrootAuthServiceImpl;
import za.org.grassroot.android.services.rest.GrassrootAuthApi;

/**
 * Created by luke on 2017/08/08.
 * todo: move this up to app scope
 */
@Module
public class AuthModule {

    @Provides
    @UserScope
    public GrassrootAuthService provideGrassrootAuthService(GrassrootAuthApi grassrootAuthApi, AccountManager accountManager) {
        return new GrassrootAuthServiceImpl(grassrootAuthApi, accountManager);
    }

}