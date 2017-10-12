package za.org.grassroot2.presenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.R;
import za.org.grassroot2.model.TokenResponse;
import za.org.grassroot2.model.exception.InvalidPhoneNumberException;
import za.org.grassroot2.model.exception.InvalidViewForPresenterException;
import za.org.grassroot2.model.exception.LifecycleOutOfSyncException;
import za.org.grassroot2.model.util.PhoneNumberUtil;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.services.rest.GrassrootAuthApi;
import za.org.grassroot2.services.rest.RestResponse;
import za.org.grassroot2.view.GrassrootView;
import za.org.grassroot2.view.LoginView;
import za.org.grassroot2.view.activity.MainActivity;

public class LoginPresenter extends BasePresenter<LoginView> {
    // public static final String PARAM_AUTHTOKEN_TYPE = "auth_token_type";
    private static final int MIN_OTP_LENGTH = 5;

    private String userName;

    private GrassrootAuthApi   grassrootAuthApi;
    private UserDetailsService userDetailsService;

    public void processInput(CharSequence value) {
        try {
            if (currentState == State.USERNAME) {
                stashUsernameAndRequestOtp(PhoneNumberUtil.convertToMsisdn(value));
            } else if (currentState == State.OTP) {
                validateOtpEntry(value);
                currentState = State.NEXT;
            }
        } catch (InvalidPhoneNumberException e) {
            Timber.e("error converting number to msisdn! : " + value);
            view.showErrorToast(R.string.error_phone_number);
        } catch (Throwable e) {
            handleNetworkConnectionError(e);
        }
    }

    private enum State {
        USERNAME, OTP, NEXT
    }


    private State currentState = State.USERNAME;

    @Inject
    public LoginPresenter(GrassrootAuthApi grassrootAuthApi,
                          UserDetailsService userDetailsService) {
        this.grassrootAuthApi = grassrootAuthApi;
        this.userDetailsService = userDetailsService;
    }

    public void attachOtp(GrassrootView passedView) {
        if (passedView == view) {
            onOtpAttached();
        } else {
            handleGenericKnownException(new InvalidViewForPresenterException());
        }
    }

    public void onViewCreated() {
        try {
            disposableOnDetach(view.usernameChanged().subscribe(charSequence -> {
                Timber.v("username changed to: " + charSequence);
                view.toggleNextButton(PhoneNumberUtil.isPossibleNumber(charSequence));
            }, java.lang.Throwable::printStackTrace));
        } catch (NullPointerException e) {
            e.printStackTrace();
            handleGenericKnownException(new LifecycleOutOfSyncException());
        }
    }

    private void onOtpAttached() {
        disposableOnDetach(view.otpChanged().subscribe(sequence -> {
            if (sequence.length() >= MIN_OTP_LENGTH) {
                view.toggleNextButton(true);
            }
        }, java.lang.Throwable::printStackTrace));
    }

    private void stashUsernameAndRequestOtp(String msisdn) {
        userName = msisdn;
        view.showProgressBar();
        disposableOnDetach(grassrootAuthApi
                .requestOtp(msisdn)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringRestResponse -> {
                    view.closeProgressBar();
                    currentState = State.OTP;
                    view.requestOtpEntry(BuildConfig.DEBUG ? stringRestResponse.getData() : null);
                }, throwable -> {
                    throwable.printStackTrace();
                    view.closeProgressBar();
                }));
    }

    private void validateOtpEntry(CharSequence charSequence) throws Throwable {
        // call the authentication service and check if these are okay, and if so, store and continue
        view.showProgressBar();
        disposableOnDetach(grassrootAuthApi
                .validateOtp(userName, "" + charSequence, AuthConstants.AUTH_CLIENT_TYPE, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::storeSuccessfulAuthAndProceed, throwable -> {
                    view.closeProgressBar();
                    throwable.printStackTrace();
                }));
    }

    private void storeSuccessfulAuthAndProceed(RestResponse<TokenResponse> response) {
        final TokenResponse tokenAndUserDetails = response.getData();
        disposableOnDetach(userDetailsService.storeUserDetails(tokenAndUserDetails.getUserUid(),
                tokenAndUserDetails.getMsisdn(),
                tokenAndUserDetails.getDisplayName(),
                tokenAndUserDetails.getSystemRole(),
                tokenAndUserDetails.getToken())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(userProfile -> {
                    view.closeProgressBar();
                    // need to pass back the JWT to the activity to ensure storage (via setResult)
                    view.loginSuccessContinue(tokenAndUserDetails.getToken(), MainActivity.class);
                }, java.lang.Throwable::printStackTrace));
    }
}
