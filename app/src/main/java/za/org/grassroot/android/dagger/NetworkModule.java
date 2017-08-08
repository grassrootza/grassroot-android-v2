package za.org.grassroot.android.dagger;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import za.org.grassroot.android.BuildConfig;
import za.org.grassroot.android.Constants;
import za.org.grassroot.android.services.rest.AddTokenInterceptor;
import za.org.grassroot.android.services.rest.CommonErrorHandlerInterceptor;
import za.org.grassroot.android.services.rest.GrassrootRestService;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class NetworkModule {

    private static final String REST_BASE_URL = "REST_BASE_URL";

    @Provides
    @Named(REST_BASE_URL)
    String provideRestBaseUrl() {
        return Constants.BASE_URL;
    }

    @Provides
    @Singleton
    Converter.Factory provideGsonConverter() {
        return GsonConverterFactory.create();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Converter.Factory converter, @Named(REST_BASE_URL) String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.BUILD_TYPE.equals("debug") ?
                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new AddTokenInterceptor()) // outbound, adds JWT if present
                .addInterceptor(new CommonErrorHandlerInterceptor()); // inbound, checks what server says about auth token

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    GrassrootRestService provideGrassrootRestService(Retrofit retrofit) {
        return retrofit.create(GrassrootRestService.class);
    }

}
