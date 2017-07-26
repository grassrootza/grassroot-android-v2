package za.org.grassroot.android.presenter;

import android.accounts.Account;
import android.accounts.AccountManager;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot.android.ApplicationLoader;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.TokenResponse;
import za.org.grassroot.android.model.exception.InvalidPhoneNumberException;
import za.org.grassroot.android.model.exception.InvalidViewForPresenterException;
import za.org.grassroot.android.model.exception.LifecycleOutOfSyncException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.util.PhoneNumberUtil;
import za.org.grassroot.android.services.auth.AuthConstants;
import za.org.grassroot.android.services.rest.GrassrootRestClient;
import za.org.grassroot.android.services.rest.RestResponse;
import za.org.grassroot.android.services.rest.RestSubscriber;
import za.org.grassroot.android.view.GrassrootView;
import za.org.grassroot.android.view.LoginView;
import za.org.grassroot.android.view.activity.MainActivity;

/**
 * Created by luke on 2017/07/06.
 */

public class LoginPresenter extends Presenter {

    public static final String PARAM_AUTHTOKEN_TYPE = "auth_token_type";
    private static final int MIN_OTP_LENGTH = 5;

    private LoginView view;
    private String userName;

    private AccountManager accountManager;

    @Override
    public void attach(GrassrootView passedView) {
        try {
            super.attach(passedView);
            view = (LoginView) passedView;
            onViewAttached();
        } catch (ClassCastException e) {
            handleGenericKnownException(new InvalidViewForPresenterException());
            super.detach(view);
        }
    }

    public void attachOtp(GrassrootView passedView) {
        if (passedView == view) {
            onOtpAttached();
        } else {
            handleGenericKnownException(new InvalidViewForPresenterException());
        }
    }

    @Override
    public void detach(GrassrootView view) {
        this.view = null;
        super.detach(view);
        onViewDetached();
    }

    @Override
    protected void onViewAttached() {
        try {
            subscriptions.add(view.usernameChanged().subscribe(new Consumer<CharSequence>() {
                @Override
                public void accept(@NonNull CharSequence charSequence) throws Exception {
                    Timber.v("username changed to: " + charSequence);
                    view.toggleNextButton(PhoneNumberUtil.isPossibleNumber(charSequence));
                }
            }));

            subscriptions.add(view.usernameNext().subscribe(new Consumer<CharSequence>() {
                @Override
                public void accept(@NonNull CharSequence charSequence) throws Exception {
                    try {
                        stashUsernameAndRequestOtp(PhoneNumberUtil.convertToMsisdn(charSequence));
                    } catch (InvalidPhoneNumberException e) {
                        Timber.e("error converting number to msisdn! : " + charSequence);
                        view.showErrorToast(R.string.error_phone_number);
                    } catch (NetworkUnavailableException e) {
                        handleNetworkConnectionError(e);
                    } catch (Exception e) {
                        handleUnknownError(e);
                    }
                }
            }));
        } catch (NullPointerException e) {
            e.printStackTrace();
            handleGenericKnownException(new LifecycleOutOfSyncException());
        }
    }

    private void onOtpAttached() {
        subscriptions.add(view.otpChanged().subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(@NonNull CharSequence sequence) throws Exception {
                if (sequence.length() >= MIN_OTP_LENGTH) {
                    view.toggleNextButton(true);
                }
            }
        }));
        subscriptions.add(view.otpEntered().subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(@NonNull CharSequence charSequence) throws Exception {
                Timber.v("otp entered! now lenght: ");
                validateOtpEntry(charSequence);
            }
        }));
    }

    @Override
    protected void onViewDetached() {
        super.onViewDetached();
        accountManager = null;
    }

    private void stashUsernameAndRequestOtp(String msisdn) {
        userName = msisdn;
        GrassrootRestClient.getService()
                .requestOtp(msisdn)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RestSubscriber<>(this, new SingleObserver<RestResponse<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Timber.v("Subscribed to OTP request rest call");
                        view.showProgressBar();
                    }

                    @Override
                    public void onSuccess(@NonNull RestResponse<String> s) {
                        Timber.v("and it's come back okay");
                        view.closeProgressBar();
                        view.requestOtpEntry(s.getData());
                    }

                    // note: network or auth errors will have been caught already
                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        view.closeProgressBar();
                    }
                }));
    }

    private void validateOtpEntry(CharSequence charSequence) throws NetworkUnavailableException {
        // call the authentication service and check if these are okay, and if so, store and continue
        GrassrootRestClient.getService()
                .validateOtp(userName, "" + charSequence, "ANDROID")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RestSubscriber<>(this, new SingleObserver<RestResponse<TokenResponse>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Timber.v("subscribed to auth request call");
                        view.showProgressBar();;
                    }

                    @Override
                    public void onSuccess(@NonNull RestResponse<TokenResponse> tokenRestResponse) {
                        Timber.v("otp came back valid"); // todo: proper checks
                        storeSuccessfulAuthAndProceed(tokenRestResponse);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        view.closeProgressBar();
                    }
                }));
    }

    private void storeSuccessfulAuthAndProceed(RestResponse<TokenResponse> tokenResponse) {
        final Account account = getOrCreateAccount();
        accountManager.setAuthToken(account, AuthConstants.AUTH_TOKENTYPE, tokenResponse.getData().getToken());
        view.closeProgressBar();
        view.showSuccessMsg("Okay it's done");
        view.loginSuccessContinue(tokenResponse.getData().getToken(), MainActivity.class);
    }

    private AccountManager getAccountManager() {
        if (accountManager == null) {
            accountManager = AccountManager.get(ApplicationLoader.applicationContext);
        }
        return accountManager;
    }

    // todo : cycle through if have multiple accounts
    private Account getOrCreateAccount() {
        Account[] accounts = getAccountManager().getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length != 0 ? accounts[0] : new Account(AuthConstants.ACCOUNT_NAME, AuthConstants.ACCOUNT_TYPE);
    }
}
