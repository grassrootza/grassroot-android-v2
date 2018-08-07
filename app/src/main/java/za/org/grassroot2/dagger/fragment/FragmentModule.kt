package za.org.grassroot2.dagger.fragment


import dagger.Module
import dagger.Provides
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.presenter.MePresenter
import za.org.grassroot2.presenter.fragment.AroundMePresenter
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.services.LocationManager
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootUserApi

@Module
class FragmentModule {

    @Provides
    @PerFragment
<<<<<<< HEAD
    internal fun provideGroupFragmentPresenter(dbService: DatabaseService, networkService: UserDetailsService, mediaService: MediaService, grassrootUserApi: GrassrootUserApi): GroupFragmentPresenter {
        return GroupFragmentPresenter(dbService, networkService, mediaService, grassrootUserApi)
=======
    internal fun provideGroupFragmentPresenter(dbService: DatabaseService, networkService: UserDetailsService): GroupFragmentPresenter {
        return GroupFragmentPresenter(dbService, networkService)
>>>>>>> 7cbe444f4e9fb9ceb76be7667ec776383fbeae4a
    }

    @Provides
    @PerFragment
    internal fun provideAroundMePresenter(manager: LocationManager, networkService: NetworkService, dbService: DatabaseService): AroundMePresenter {
        return AroundMePresenter(manager, networkService, dbService)
    }

    @Provides
    @PerFragment
    internal fun provideMePresenter(dbService: DatabaseService, mediaService: MediaService, grassrootUserApi: GrassrootUserApi, userDetailsService: UserDetailsService): MePresenter {
        return MePresenter(dbService, mediaService, userDetailsService, grassrootUserApi)
    }

}
