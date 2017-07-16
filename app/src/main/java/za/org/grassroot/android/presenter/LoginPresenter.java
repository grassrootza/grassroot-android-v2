package za.org.grassroot.android.presenter;

import android.util.Log;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.exception.InvalidPhoneNumberException;
import za.org.grassroot.android.model.exception.InvalidViewForPresenterException;
import za.org.grassroot.android.model.exception.LifecycleOutOfSyncException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.util.PhoneNumberUtil;
import za.org.grassroot.android.services.rest.GrassrootRestClient;
import za.org.grassroot.android.services.rest.RestResponse;
import za.org.grassroot.android.services.rest.RestSubscriber;
import za.org.grassroot.android.view.GrassrootView;
import za.org.grassroot.android.view.LoginView;

/**
 * Created by luke on 2017/07/06.
 */

public class LoginPresenter extends Presenter {

    private static final String TAG = LoginPresenter.class.getSimpleName();

    private LoginView view;
    private String userName;

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
                    view.toggleNextButton(PhoneNumberUtil.isPossibleNumber(charSequence));
                }
            }));

            subscriptions.add(view.usernameNext().subscribe(new Consumer<CharSequence>() {
                @Override
                public void accept(@NonNull CharSequence charSequence) throws Exception {
                    try {
                        stashUsernameAndRequestOtp(PhoneNumberUtil.convertToMsisdn(charSequence));
                    } catch (InvalidPhoneNumberException e) {
                        Log.e(TAG, "error converting number to msisdn! : " + charSequence);
                        view.showErrorToast(R.string.error_phone_number);
                    } catch (NetworkUnavailableException e) {
                        handleNetworkConnectionError(e);
                    } catch (Exception e) {
                        handleUnknownError(e);
                    }
                }
            }));

            subscriptions.add(view.otpEntered().subscribe(new Consumer<CharSequence>() {
                @Override
                public void accept(@NonNull CharSequence charSequence) throws Exception {

                }
            }));
        } catch (NullPointerException e) {
            handleGenericKnownException(new LifecycleOutOfSyncException());
        }
    }

    @Override
    protected void onViewDetached() {
        super.onViewDetached();
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
                        Log.v(TAG, "Subscribed to OTP request rest call");
                        view.showProgressBar();
                    }

                    @Override
                    public void onSuccess(@NonNull RestResponse<String> s) {
                        Log.v(TAG, "and it's come back okay");
                        view.closeProgressBar();
                        view.requestOtpEntry(s.getData());
                    }

                    // note: network or auth errors will have been caught already
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "there was an error");
                        e.printStackTrace();
                        view.closeProgressBar();
                    }
                }));
    }

    private void validateOtpEntry(CharSequence charSequence) throws NetworkUnavailableException {
        // call the authentication service and check if these are okay, and if so, store and continue
    }
}
