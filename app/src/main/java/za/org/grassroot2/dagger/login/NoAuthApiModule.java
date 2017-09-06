package za.org.grassroot2.dagger.login;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import za.org.grassroot2.Constants;
import za.org.grassroot2.services.rest.GrassrootAuthApi;

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
