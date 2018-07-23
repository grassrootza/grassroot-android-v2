package za.org.grassroot2.view.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import za.org.grassroot.messaging.dto.MessageDTO;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.AppComponent;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.activity.ActivityModule;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.service.GCMPreferences;
import za.org.grassroot2.services.OfflineReceiver;
import za.org.grassroot2.services.SyncOfflineDataService;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.services.rest.AddTokenInterceptor;
import za.org.grassroot2.util.AlarmManagerHelper;
import za.org.grassroot2.util.UserPreference;
import za.org.grassroot2.util.ViewAnimation;
import za.org.grassroot2.view.GrassrootView;
import za.org.grassroot2.view.dialog.GenericErrorDialog;
import za.org.grassroot2.view.dialog.GenericMessageDialog;
import za.org.grassroot2.view.dialog.GenericSuccessDialog;
import za.org.grassroot2.view.dialog.NoConnectionDialog;

public abstract class GrassrootActivity extends AppCompatActivity implements GrassrootView {

    protected static final String DIALOG_TAG = "dialog";
    @Inject public Lazy<AccountManager> accountManagerProvider;
    @Inject public UserPreference       userPreference;
    @Inject
    public ObjectMapper jsonMaper;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;


    private   AccountAuthenticatorResponse authResponse     = null;
    private   Bundle                       authResultBundle = null;
    protected CompositeDisposable          disposables      = new CompositeDisposable();

    @BindView(R.id.progress)
    @Nullable
    View progress;
    private ActivityComponent component;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.base_progress_container);
        setContentLayout(getLayoutResourceId());
        ButterKnife.bind(this);
        getComponenet().inject(this);
        onInject(getComponenet());
        authResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (authResponse != null) {
            authResponse.onRequestContinued();
        }


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String currentToken = sharedPreferences.getString(GCMPreferences.CURRENT_GCM_TOKEN, null);
                Timber.i("GCM token check finished. Current token: %s", currentToken);
                closeProgressBar();
            }
        };
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(GCMPreferences.GCM_REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
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
    public void showMessageDialog(String text) {
        DialogFragment dialog = GenericMessageDialog.newInstance(text);
        dialog.show(getSupportFragmentManager(), DIALOG_TAG);
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
                dialog = NoConnectionDialog.Companion.newInstance(NoConnectionDialog.TYPE_AUTHORIZED);
            } else {
                dialog = NoConnectionDialog.Companion.newInstance(NoConnectionDialog.TYPE_NOT_AUTHORIZED);
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
    public void showSuccessSnackbar(int successMsg) {
        Snackbar.make(findViewById(android.R.id.content), successMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorDialog(int errorMsg) {
        DialogFragment dialog = GenericErrorDialog.Companion.newInstance(errorMsg);
        dialog.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    @Override
    public void showErrorSnackbar(int stringResId) {
        Snackbar.make(findViewById(android.R.id.content), stringResId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessDialog(int titleRes, @NotNull View.OnClickListener okayListener) {
        DialogFragment dialog = GenericSuccessDialog.newInstance(titleRes, okayListener);
        dialog.show(getSupportFragmentManager(), DIALOG_TAG);
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
        Timber.d("showing progress bar 1 in activity: %s", getActivity().toString());
        if (progress != null) {
            ViewAnimation.fadeIn(progress);
        }
    }

    @Override
    public void closeProgressBar() {
        Timber.d("showing progress bar 2 inside activity");
        if (progress != null) {
            ViewAnimation.fadeOut(progress);
        }
    }

    public ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    public ActivityComponent getComponenet() {
        if (component == null) {
            component = getAppComponent().plus(getActivityModule());
        }
        return component;
    }

    public AppComponent getAppComponent() {
        return ((GrassrootApplication) getApplication()).getAppComponent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
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

//        if (loggedIn()) {
//
//            if (checkPlayServices()) {
//                Timber.e("Showing progress bar 3 in activity: %s", getActivity().toString());
//                // start registration service in order to check token and register if not already registered
//                showProgressBar();
//                registerReceiver();
//                Intent intent = new Intent(this, GCMRegistrationService.class);
//                startService(intent);
//            }
//        }
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
        if (e.syncable instanceof Meeting) {
            Snackbar.make(findViewById(android.R.id.content), e.msg, Snackbar.LENGTH_LONG).show();
        }
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


    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.i("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void sendCGMMessage(MessageDTO messageDTO) {
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            Bundle data = new Bundle();
            String msgJson = jsonMaper.writeValueAsString(messageDTO);
            data.putString("body", msgJson);

            String id = UUID.randomUUID().toString();
            String senderId = getString(R.string.gcm_sender_id);
            gcm.send(senderId + "@gcm.googleapis.com", id, data);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }
}
