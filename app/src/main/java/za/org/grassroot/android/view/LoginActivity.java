package za.org.grassroot.android.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.rxbinding.RxTextView;
import za.org.grassroot.android.rxbinding.RxTextViewUtils;
import za.org.grassroot.android.rxbinding.RxView;
import za.org.grassroot.android.view.activity.GrassrootActivity;

/**
 * Created by luke on 2017/07/07.
 */

public class LoginActivity extends GrassrootActivity implements LoginView {

    private LoginPresenter loginPresenter;
    private ProgressDialog progressDialog;

    @BindView(R.id.login_username) EditText userNameEditText;
    @BindView(R.id.login_next) Button nextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (loginPresenter == null) {
            loginPresenter = new LoginPresenter();
        }
        loginPresenter.attach(this);
        nextButton.setEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loginPresenter.detach(this);
    }

    @Override
    public Observable<CharSequence> usernameChanged() {
        return RxTextView.textChanges(userNameEditText);
    }

    @Override
    public Observable<CharSequence> usernameNext() {
        Observable<CharSequence> editTextNext = RxTextView
                .editorActions(userNameEditText, RxTextViewUtils.imeNextDonePredicate())
                .map(new Function<Integer, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Integer integer) throws Exception {
                        return userNameEditText.getText();
                    }
                });
        Observable<CharSequence> nextButtonClicked = RxView
                .clicks(nextButton).map(new Function<Object, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Object o) throws Exception {
                        return userNameEditText.getText();
                    }
                });
        return Observable.merge(editTextNext, nextButtonClicked);
    }

    @Override
    public Observable<CharSequence> otpEntered() {
        return null;
    }

    @Override
    public void toggleNextButton(boolean enabled) {
        nextButton.setEnabled(enabled);
    }

    @Override
    public void requestOtpEntry() {

    }

    @Override
    public void displayInvalidUsername() {

    }

    @Override
    public void displayInvalidOtp() {

    }

    @Override
    public Observable<ConnectionResult> showConnectionFailedDialog() {
        return null;
    }

    @Override
    public Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog() {
        return null;
    }

    @Override
    public void showProgressBar() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Hello this is a progress dialog");
        }
        progressDialog.show();
    }

    @Override
    public void closeProgressBar() {
        progressDialog.cancel();
    }
}
