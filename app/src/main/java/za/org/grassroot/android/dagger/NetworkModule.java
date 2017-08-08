package za.org.grassroot.android.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.converter.gson.GsonConverterFactory;
import za.org.grassroot.android.BuildConfig;
import za.org.grassroot.android.services.rest.CommonErrorHandlerInterceptor;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class NetworkModule {

    @Provides
    @Singleton
    Converter.Factory provideGsonConverter() {
        return GsonConverterFactory.create();
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.BUILD_TYPE.equals("debug") ?
                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);
        return logging;
    }

    @Provides
    @Singleton
    CommonErrorHandlerInterceptor provideErrorInterceptor() {
        return new CommonErrorHandlerInterceptor();
    }

    @Provides
    @Singleton
    OkHttpClient provideHttpClient(HttpLoggingInterceptor loggingInterceptor,
                                   CommonErrorHandlerInterceptor errorInterceptor) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(errorInterceptor); // inbound, checks what server says about auth token
        return httpClient.build();
    }
}
