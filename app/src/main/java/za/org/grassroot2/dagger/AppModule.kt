package za.org.grassroot2.dagger

import android.accounts.AccountManager
import android.app.Application
import android.content.Context

import com.fasterxml.jackson.databind.ObjectMapper

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import za.org.grassroot2.database.DatabaseHelper
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.database.DatabaseServiceImpl
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.UserDetailsServiceImpl
import za.org.grassroot2.util.ContactHelper
import za.org.grassroot2.util.UserPreference

/**
 * Created by luke on 2017/08/08.
 */
@Module
class AppModule(private val application: Application) {

    @Provides
    @ApplicationContext
    fun provideContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun providesAccountManager(@ApplicationContext context: Context): AccountManager {
        return AccountManager.get(context)
    }

    @Provides
    @Singleton
    fun providesContactHelper(@ApplicationContext context: Context): ContactHelper {
        return ContactHelper(context)
    }

    @Provides
    @Singleton
    internal fun provideDatabase(helper: DatabaseHelper): DatabaseService {
        return DatabaseServiceImpl(helper)
    }

    @Provides
    @Singleton
    internal fun provideUserPreference(@ApplicationContext context: Context): UserPreference {
        return UserPreference(context)
    }

    @Provides
    @Singleton
    internal fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper {
        return DatabaseHelper(context)
    }

    @Provides
    @Singleton
    internal fun provideUserDetailsService(accountManager: AccountManager, databaseService: DatabaseService): UserDetailsService {
        return UserDetailsServiceImpl(accountManager, databaseService)
    }


    @Provides
    @Singleton
    internal fun provideObjectMapper(): ObjectMapper {
        return ObjectMapper()
    }
}
