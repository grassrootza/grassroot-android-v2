package za.org.grassroot.android.dagger.user;

import android.accounts.AccountManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import za.org.grassroot.android.Constants;
import za.org.grassroot.android.services.NetworkService;
import za.org.grassroot.android.services.NetworkServiceImpl;
import za.org.grassroot.android.services.RealmService;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.services.rest.AddTokenInterceptor;
import za.org.grassroot.android.services.rest.GrassrootUserApi;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class ApiModule {

    private static final String REST_BASE_URL = "REST_BASE_URL";

    @Provides
    @Named(REST_BASE_URL)
    String provideRestBaseUrl() {
        return Constants.BASE_URL;
    }

    @Provides
    @UserScope
    AddTokenInterceptor provideTokenInterceptor(AccountManager accountManager) {
        return new AddTokenInterceptor(accountManager);
    }

    @Provides
    @UserScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient,
                             Converter.Factory converter,
                             AddTokenInterceptor tokenInterceptor,
                             @Named(REST_BASE_URL) String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient.newBuilder().addInterceptor(tokenInterceptor).build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converter)
                .build();
    }

    @Provides
    @UserScope
    GrassrootUserApi provideGrassrootRestService(Retrofit retrofit) {
        return retrofit.create(GrassrootUserApi.class);
    }

    @Provides
    @UserScope
    NetworkService provideNetworkService(UserDetailsService userDetailsService,
                                         GrassrootUserApi grassrootUserApi,
                                         RealmService realmService) {
        return new NetworkServiceImpl(userDetailsService, grassrootUserApi, realmService);
    }

}
