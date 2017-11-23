package za.org.grassroot2.dagger.fragment;


import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.presenter.MePresenter;
import za.org.grassroot2.presenter.fragment.AroundMePresenter;
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter;
import za.org.grassroot2.services.LocationManager;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.rest.GrassrootUserApi;

@Module
public class FragmentModule {

    @Provides
    @PerFragment
    GroupFragmentPresenter provideGroupFragmentPresenter(DatabaseService dbService, UserDetailsService networkService) {
        return new GroupFragmentPresenter(dbService, networkService);
    }

    @Provides
    @PerFragment
    AroundMePresenter provideAroundMePresenter(LocationManager manager, NetworkService networkService, DatabaseService dbService) {
        return new AroundMePresenter(manager, networkService, dbService);
    }

    @Provides
    @PerFragment
    MePresenter provideMePresenter(DatabaseService dbService, MediaService mediaService, GrassrootUserApi grassrootUserApi, UserDetailsService userDetailsService) {
        return new MePresenter(dbService, mediaService, userDetailsService, grassrootUserApi);
    }
}
