package za.org.grassroot2.dagger

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.services.rest.GrassrootAuthApi

/**
 * Created by luke on 2017/08/08.
 */
@Module
class NoAuthApiModule {

    @Provides
    @NonAuthorized
    internal fun provideRetrofit(okHttpClient: OkHttpClient,
                                 converter: Converter.Factory): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converter)
                .build()
    }

    @Provides
    @Singleton
    internal fun provideGrassrootAuthApi(@NonAuthorized retrofit: Retrofit): GrassrootAuthApi {
        return retrofit.create(GrassrootAuthApi::class.java)
    }


}
