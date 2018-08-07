package za.org.grassroot2.dagger.user

import android.accounts.AccountManager
import android.content.Context

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.dagger.ApplicationContext
import za.org.grassroot2.dagger.Authorized
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.NetworkServiceImpl
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.AddTokenInterceptor
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.util.UserPreference

/**
 * Created by luke on 2017/08/08.
 */
@Module
class ApiModule {

    @Provides
    internal fun provideTokenInterceptor(@ApplicationContext c: Context, accountManager: AccountManager, userPref: UserPreference): AddTokenInterceptor {
        return AddTokenInterceptor(c, accountManager, userPref)
    }

    @Provides
    @Authorized
    internal fun provideRetrofit(okHttpClient: OkHttpClient,
                                 converter: Converter.Factory,
                                 tokenInterceptor: AddTokenInterceptor): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE)
                .client(okHttpClient.newBuilder().addInterceptor(tokenInterceptor).build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converter)
                .build()
    }

    @Provides
    internal fun provideGrassrootRestService(@Authorized retrofit: Retrofit): GrassrootUserApi {
        return retrofit.create(GrassrootUserApi::class.java)
    }

    @Provides
    internal fun provideNetworkService(grassrootUserApi: GrassrootUserApi,
                                       realmService: DatabaseService): NetworkService {
        return NetworkServiceImpl(grassrootUserApi, realmService)
    }

}
