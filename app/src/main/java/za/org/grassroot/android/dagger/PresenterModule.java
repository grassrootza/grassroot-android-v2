package za.org.grassroot.android.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;
import za.org.grassroot.android.presenter.LoginPresenter;

/**
 * Created by luke on 2017/08/08.
 */
@Module
public class PresenterModule {

    @Provides
    @Singleton
    LoginPresenter provideLoginPresenter(Context context) {
        Timber.e("called provide login presenter");
        return new LoginPresenter(context); // for now, switch to implementation pattern soon
    }

}
