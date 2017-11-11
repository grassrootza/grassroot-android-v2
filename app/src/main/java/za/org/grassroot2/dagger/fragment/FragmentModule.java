package za.org.grassroot2.dagger.fragment;

import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import za.org.grassroot2.dagger.ActivityContext;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.presenter.AroundMePresenter;
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter;
import za.org.grassroot2.services.LocationManager;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.UserDetailsService;

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

}
