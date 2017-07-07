package za.org.grassroot.android.view;

import android.os.Bundle;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.view.activity.GrassrootActivity;

/**
 * Created by luke on 2017/07/07.
 */

public class LoginActivity extends GrassrootActivity implements LoginView {

    private LoginPresenter loginPresenter;

    @BindView(R.id.login_username) EditText userNameEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (loginPresenter == null) {
            loginPresenter = new LoginPresenter();
        }
        loginPresenter.attach(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loginPresenter.detach(this);
    }

    @Override
    public Observable<CharSequence> usernameEntered() {
        // return RxTextView.editorActionEvents(userNameEditText);
        return null;
    }

    @Override
    public Observable<CharSequence> otpEntered() {
        return null;
    }

    @Override
    public void requestOtpEntry() {

    }

    @Override
    public Observable<ConnectionResult> showConnectionFailedDialog() {
        return null;
    }

    @Override
    public Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog() {
        return null;
    }
}
