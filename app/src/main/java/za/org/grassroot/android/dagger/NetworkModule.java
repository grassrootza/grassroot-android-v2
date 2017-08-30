package za.org.grassroot.android.dagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmList;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.converter.gson.GsonConverterFactory;
import za.org.grassroot.android.BuildConfig;
import za.org.grassroot.android.model.helper.RealmString;
import za.org.grassroot.android.model.helper.StringRealmListConverter;
import za.org.grassroot.android.services.rest.CommonErrorHandlerInterceptor;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class NetworkModule {

    @Provides
    @Singleton
    Converter.Factory provideGsonConverter() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {}.getType(),
                        new StringRealmListConverter())
                .create();
        return GsonConverterFactory
                .create(gson);
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.BUILD_TYPE.equals("debug") ?
                HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.BASIC);
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
