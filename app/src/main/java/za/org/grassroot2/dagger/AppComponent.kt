package za.org.grassroot2.dagger

import javax.inject.Singleton

import dagger.Component
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.dagger.activity.ActivityModule
import za.org.grassroot2.dagger.user.ApiModule
import za.org.grassroot2.service.GCMRegistrationService
import za.org.grassroot2.services.OfflineReceiver
import za.org.grassroot2.services.SyncOfflineDataService
import za.org.grassroot2.services.account.AccountAuthenticator
import za.org.grassroot2.services.account.GrassrootAuthService
import za.org.grassroot2.services.account.SyncAdapter

/**
 * Created by luke on 2017/08/08.
 */
@Singleton
@Component(modules = [(AppModule::class), (NetworkModule::class), (ApiModule::class), (NoAuthApiModule::class)])
interface AppComponent {

    fun inject(syncAdapter: SyncAdapter)
    fun inject(service: GrassrootAuthService)
    fun inject(accountAuthenticator: AccountAuthenticator)
    fun inject(offlineReceiver: OfflineReceiver)
    fun inject(syncOfflineDataService: SyncOfflineDataService)

    fun inject(gcmRegistrationService: GCMRegistrationService)

    operator fun plus(activityModule: ActivityModule): ActivityComponent
}