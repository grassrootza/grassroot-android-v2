package za.org.grassroot2.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.model.TokenResponse
import za.org.grassroot2.model.util.PhoneNumberUtil
import za.org.grassroot2.presenter.activity.BasePresenter
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootAuthApi
import za.org.grassroot2.services.rest.RestResponse
import za.org.grassroot2.view.LoginView
import za.org.grassroot2.view.activity.DashboardActivity
import java.net.ConnectException
import javax.inject.Inject

class LoginPresenter @Inject constructor(val grassrootAuthApi: GrassrootAuthApi,
                                         val userDetailsService: UserDetailsService) : BasePresenter<LoginView>() {

    fun validatePhoneNumber(phoneNumber: CharSequence) {
        val numberOk = PhoneNumberUtil.isPossibleNumber(phoneNumber)
        view.toggleSubmitButton(numberOk)
    }


    fun login(phoneNumber: String, password: String) {

        view.showProgressBar()
        disposableOnDetach(grassrootAuthApi
                .login(phoneNumber, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::storeSuccessfulAuthAndProceed, this::handleLoginError)
        )
    }


    private fun storeSuccessfulAuthAndProceed(response: RestResponse<TokenResponse>) {
        val tokenAndUserDetails = response.data

        disposableOnDetach(
                userDetailsService.storeUserDetails(tokenAndUserDetails.userUid,
                        tokenAndUserDetails.msisdn,
                        tokenAndUserDetails.displayName,
                        tokenAndUserDetails.email,
                        tokenAndUserDetails.languageCode,
                        tokenAndUserDetails.systemRole,
                        tokenAndUserDetails.token)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { storeDetailSuccess(tokenAndUserDetails) },
                                { storeDetailsFailed(it) }
                        )
        )
    }


    private fun storeDetailsFailed(it: Throwable) {
        view.closeProgressBar()
        Timber.e(it)
    }

    private fun storeDetailSuccess(tokenAndUserDetails: TokenResponse) {
        view.closeProgressBar()
        view.loginSuccessContinue(tokenAndUserDetails.token, DashboardActivity::class.java)
    }

    private fun handleLoginError(it: Throwable) {
        view.closeProgressBar()
        if (it is ConnectException)
            view.showNoConnectionMessage()
        else
            view.showErrorSnackbar(R.string.login_failed)
        it.printStackTrace()
    }
}