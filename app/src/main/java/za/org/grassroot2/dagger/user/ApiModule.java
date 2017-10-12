package za.org.grassroot2.dagger.user;

import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.dagger.Authorized;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.NetworkServiceImpl;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.deserializer.TaskDeserlializer;
import za.org.grassroot2.services.rest.AddTokenInterceptor;
import za.org.grassroot2.services.rest.GrassrootUserApi;
import za.org.grassroot2.util.UserPreference;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class ApiModule {

    @Provides
    AddTokenInterceptor provideTokenInterceptor(@ApplicationContext Context c, AccountManager accountManager, UserPreference userPref) {
        return new AddTokenInterceptor(c, accountManager, userPref);
    }

    @Provides
    @Authorized
    Retrofit provideRetrofit(OkHttpClient okHttpClient,
                             Converter.Factory converter,
                             AddTokenInterceptor tokenInterceptor) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE)
                .client(okHttpClient.newBuilder().addInterceptor(tokenInterceptor).build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converter)
                .build();
    }

    @Provides
    GrassrootUserApi provideGrassrootRestService(@Authorized Retrofit retrofit) {
        return retrofit.create(GrassrootUserApi.class);
    }

    @Provides
    NetworkService provideNetworkService(UserDetailsService userDetailsService,
                                         GrassrootUserApi grassrootUserApi,
                                         DatabaseService realmService) {
        return new NetworkServiceImpl(userDetailsService, grassrootUserApi, realmService);
    }

}
