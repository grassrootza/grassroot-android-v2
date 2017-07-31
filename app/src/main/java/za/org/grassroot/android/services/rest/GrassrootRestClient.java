package za.org.grassroot.android.services.rest;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import za.org.grassroot.android.BuildConfig;

public class GrassrootRestClient {

    private static Retrofit retrofit = null;
    private static GrassrootRestService service = null;

    private static final String baseUrl = "http://10.0.2.2:8080/api/";

    private static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.BUILD_TYPE.equals("debug") ?
                    HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new AddTokenInterceptor()) // outbound, adds JWT if present
                    .addInterceptor(new CommonErrorHandlerInterceptor()); // inbound, checks what server says about auth token

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
                    service = methodInstance = getClient(baseUrl).create(GrassrootRestService.class);
                }
            }
        }
        return methodInstance;
    }

}
