package za.org.grassroot2.dagger

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit

import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.model.AroundItem
import za.org.grassroot2.model.ExcludeFromSerialization
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.services.deserializer.AroundResponseDeserializer
import za.org.grassroot2.services.deserializer.TaskDeserlializer
import za.org.grassroot2.services.rest.CommonErrorHandlerInterceptor

@Module
class NetworkModule {

    private// Create a trust manager that does not validate certificate chains, for use only in development
    //            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.180", 8888));
    //            builder.proxy(proxy);
    val unsafeOkHttpClient: OkHttpClient
        get() {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { hostname, session -> true }
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
                builder.retryOnConnectionFailure(true)
                return builder.build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }

    @Provides
    @Singleton
    internal fun provideGsonConverter(): Converter.Factory {
        val gson = GsonBuilder().registerTypeAdapter(Task::class.java, TaskDeserlializer())
                .registerTypeAdapter(AroundItem::class.java, AroundResponseDeserializer())
                .addSerializationExclusionStrategy(object : ExclusionStrategy {
                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        return f.getAnnotation(ExcludeFromSerialization::class.java) != null
                    }

                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }
                })
                .create()
        return GsonConverterFactory
                .create(gson)
    }

    @Provides
    @Singleton
    internal fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.BASIC
        return logging
    }

    @Provides
    @Singleton
    internal fun provideErrorInterceptor(): CommonErrorHandlerInterceptor {
        return CommonErrorHandlerInterceptor()
    }

    @Provides
    @Singleton
    internal fun provideHttpClient(loggingInterceptor: HttpLoggingInterceptor,
                                   errorInterceptor: CommonErrorHandlerInterceptor): OkHttpClient {
        if (BuildConfig.USE_UNSAFE_HTTP) {
            return unsafeOkHttpClient
        } else {
            val httpClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(errorInterceptor) // inbound, checks what server says about auth token
            httpClient.writeTimeout(5, TimeUnit.MINUTES)
            return httpClient.build()
        }
    }
}
