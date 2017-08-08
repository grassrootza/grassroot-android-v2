package za.org.grassroot.android.dagger.login;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import za.org.grassroot.android.Constants;
import za.org.grassroot.android.services.rest.GrassrootAuthApi;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class NoAuthApiModule {

    private static final String AUTH_BASE_URL = "AUTH_BASE_URL";

    @Provides
    @Named(AUTH_BASE_URL)
    String provideRestBaseUrl() {
        return Constants.BASE_URL;
    }

    @Provides
    @LoginScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient,
                             Converter.Factory converter,
                             @Named(AUTH_BASE_URL) String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converter)
                .build();
    }

    @Provides
    @LoginScope
    GrassrootAuthApi provideGrassrootAuthApi(Retrofit retrofit) {
        return retrofit.create(GrassrootAuthApi.class);
    }


}
