package za.org.grassroot2.view.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import io.reactivex.disposables.CompositeDisposable;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.AppComponent;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.activity.ActivityModule;
import za.org.grassroot2.services.OfflineReceiver;
import za.org.grassroot2.services.SyncOfflineDataService;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.services.rest.AddTokenInterceptor;
import za.org.grassroot2.util.AlarmManagerHelper;
import za.org.grassroot2.util.UserPreference;
import za.org.grassroot2.util.ViewAnimation;
import za.org.grassroot2.view.GrassrootView;
import za.org.grassroot2.view.dialog.NoConnectionDialog;

public abstract class GrassrootActivity extends AppCompatActivity implements GrassrootView {

    protected static final String DIALOG_TAG = "dialog";
    @Inject public Lazy<AccountManager> accountManagerProvider;
    @Inject public UserPreference       userPreference;

    private   AccountAuthenticatorResponse authResponse     = null;
    private   Bundle                       authResultBundle = null;
    protected CompositeDisposable          disposables      = new CompositeDisposable();

    @BindView(R.id.progress)
    @Nullable
    View progress;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.base_progress_container);
        setContentLayout(getLayoutResourceId());
        ButterKnife.bind(this);
        getActivityComponent().inject(this);
        onInject(getActivityComponent());
        authResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (authResponse != null) {
            authResponse.onRequestContinued();
        }
    }

    private ActivityComponent getActivityComponent() {
        return getAppComponent().plus(getActivityModule());
    }

    protected abstract void onInject(ActivityComponent component);

    @LayoutRes
    protected abstract int getLayoutResourceId();

    private void setContentLayout(int resId) {
        RelativeLayout parent = (RelativeLayout) findViewById(R.id.main_layout);
        View v = LayoutInflater.from(this).inflate(resId, parent, false);
        parent.addView(v);
    }

    protected boolean loggedIn() {
        Account[] accounts = accountManagerProvider.get().getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length != 0 && !TextUtils.isEmpty(accountManagerProvider.get().getUserData(accounts[0], AuthConstants.USER_DATA_LOGGED_IN));
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void handleNoConnection() {
        if (!userPreference.connectionInfoDisplayed()) {
            DialogFragment dialog;
            if (loggedIn()) {
                dialog = NoConnectionDialog.newInstance(NoConnectionDialog.TYPE_AUTHORIZED);
            } else {
                dialog = NoConnectionDialog.newInstance(NoConnectionDialog.TYPE_NOT_AUTHORIZED);
            }
            dialog.show(getSupportFragmentManager(), DIALOG_TAG);
            userPreference.setNoConnectionInfoDisplayed(true);
            AlarmManagerHelper.scheduleAlarmForBroadcastReceiver(this, OfflineReceiver.class);
        }
    }

    @Override
    public void handleNoConnectionUpload() {
        if (userPreference.connectionInfoDisplayed()) {
            showNoConnectionMessage();
        } else {
            handleNoConnection();
        }
    }

    @Override
    public void showSuccessMsg(int successMsg) {
        Snackbar.make(findViewById(android.R.id.content), successMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorSnackbar(int stringResId) {
        Snackbar.make(findViewById(android.R.id.content), stringResId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void launchActivity(Class<?> cls, Bundle args) {
        Intent i = new Intent(this, cls);
        if (args != null) {
            i.putExtras(args);
        }
        startActivity(i);
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        authResultBundle = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    @Override
    public void finish() {
        if (authResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (authResultBundle != null) {
                authResponse.onResult(authResultBundle);
            } else {
                authResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            authResponse = null;
        }
        super.finish();
    }

    @Override
    public void closeKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void showProgressBar() {
        if (progress != null) {
            ViewAnimation.fadeIn(progress);
        }
    }

    @Override
    public void closeProgressBar() {
        if (progress != null) {
            ViewAnimation.fadeOut(progress);
        }
    }

    public ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    public AppComponent getAppComponent() {
        return ((GrassrootApplication) getApplication()).getAppComponent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialogs();
        if (!disposables.isDisposed()) {
            disposables.clear();
        }
    }

    private void dismissDialogs() {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (fragmentByTag != null) {
            ((DialogFragment) fragmentByTag).dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void tokenRefreshEvent(AddTokenInterceptor.TokenRefreshEvent e) {
        EventBus.getDefault().removeStickyEvent(e);
        AccountManager accountManager = accountManagerProvider.get();
        final Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        accountManager.getAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE, null, this, future -> {
            try {
                if (future.getResult().containsKey(AccountManager.KEY_AUTHTOKEN)) {
                    accountManager.setAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE, future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                    accountManager.setUserData(accounts[0], AuthConstants.USER_DATA_CURRENT_TOKEN, future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                }
            } catch (OperationCanceledException | IOException | AuthenticatorException e1) {
                e1.printStackTrace();
            }
        }, null);
    }

    @Subscribe
    public void notifyItemOutOfSync(SyncOfflineDataService.ObjectOutOfSyncEvent e) {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.meeting_out_of_sync, e.syncable.getName()), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showNoConnectionMessage() {
        Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_offline, Snackbar.LENGTH_SHORT).show();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
