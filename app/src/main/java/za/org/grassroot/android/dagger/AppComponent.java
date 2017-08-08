package za.org.grassroot.android.dagger;

import javax.inject.Singleton;

import dagger.Component;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.services.auth.AccountAuthenticator;
import za.org.grassroot.android.view.LoginActivity;

/**
 * Created by luke on 2017/08/08.
 */
@Singleton
@Component(modules = {AppModule.class, PresenterModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(LoginActivity target);

    void inject(LoginPresenter target);

    void inject(AccountAuthenticator target);

}