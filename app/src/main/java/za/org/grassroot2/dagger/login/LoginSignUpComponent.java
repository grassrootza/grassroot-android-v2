package za.org.grassroot2.dagger.login;

import dagger.Subcomponent;
import za.org.grassroot2.services.account.GrassrootAuthService;
import za.org.grassroot2.view.LoginActivity;

/**
 * Created by luke on 2017/08/08.
 */
@LoginScope
@Subcomponent(modules = {NoAuthApiModule.class})
public interface LoginSignUpComponent {

    void inject(GrassrootAuthService grassrootAuthService);
    void inject(LoginActivity target);

}
