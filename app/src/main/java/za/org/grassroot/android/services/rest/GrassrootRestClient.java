package za.org.grassroot.android.services.rest;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import za.org.grassroot.android.BuildConfig;

/**
 * Created by luke on 2017/07/12.
 */

public class GrassrootRestClient {

    private static Retrofit retrofit = null;
    private static GrassrootRestService service = null;

    private static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.BUILD_TYPE.equals("debug") ?
                    HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AddTokenInterceptor()) // outbound, adds JWT if present
                    .addInterceptor(new CheckAuthResponseInterceptor()); // inbound, checks what server says about auth token

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static GrassrootRestService getService() {
        GrassrootRestService methodInstance = service;
        if (methodInstance == null) {
            synchronized (GrassrootRestClient.class) {
                methodInstance = service;
                if (methodInstance == null) {
                    service = methodInstance = getClient("API").create(GrassrootRestService.class);
                }
            }
        }
        return methodInstance;
    }

}
