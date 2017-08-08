package za.org.grassroot.android.dagger.login;

import dagger.Subcomponent;
import za.org.grassroot.android.view.LoginActivity;

/**
 * Created by luke on 2017/08/08.
 */
@LoginScope
@Subcomponent(modules = {NoAuthApiModule.class})
public interface LoginSignUpComponent {

    void inject(LoginActivity target);

}
