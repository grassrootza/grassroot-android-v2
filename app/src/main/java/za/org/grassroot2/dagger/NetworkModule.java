package za.org.grassroot2.dagger;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.converter.gson.GsonConverterFactory;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.model.AroundItem;
import za.org.grassroot2.model.ExcludeFromSerialization;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.services.deserializer.AroundResponseDeserializer;
import za.org.grassroot2.services.deserializer.TaskDeserlializer;
import za.org.grassroot2.services.rest.CommonErrorHandlerInterceptor;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    Converter.Factory provideGsonConverter() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Task.class, new TaskDeserlializer())
                .registerTypeAdapter(AroundItem.class, new AroundResponseDeserializer())
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getAnnotation(ExcludeFromSerialization.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        return GsonConverterFactory
                .create(gson);
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BODY: HttpLoggingInterceptor.Level.BASIC);
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
        if (BuildConfig.USE_UNSAFE_HTTP) {
            return getUnsafeOkHttpClient();
        } else {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(errorInterceptor); // inbound, checks what server says about auth token
            httpClient.writeTimeout(5, TimeUnit.MINUTES);
            return httpClient.build();
        }
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.180", 8888));
//            builder.proxy(proxy);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
            builder.retryOnConnectionFailure(true);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
