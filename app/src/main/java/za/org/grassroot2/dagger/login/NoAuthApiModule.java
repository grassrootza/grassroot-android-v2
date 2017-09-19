package za.org.grassroot2.dagger.login;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.services.rest.GrassrootAuthApi;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class NoAuthApiModule {

    @Provides
    @LoginScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient,
                             Converter.Factory converter) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE)
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
