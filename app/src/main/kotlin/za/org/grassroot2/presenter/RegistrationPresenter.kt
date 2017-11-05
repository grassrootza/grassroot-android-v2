package za.org.grassroot2.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.R
import za.org.grassroot2.model.TokenResponse
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootAuthApi
import za.org.grassroot2.services.rest.RestResponse
import za.org.grassroot2.view.RegistrationView
import za.org.grassroot2.view.activity.DashboardActivity
import javax.inject.Inject

class RegistrationPresenter @Inject constructor(val grassrootAuthApi: GrassrootAuthApi,
                                                val userDetailsService: UserDetailsService) : BasePresenter<RegistrationView>() {

    private var userName: String = ""
    private var phoneNumber: String = ""
    private var otpCode: String = ""
    private var userProfile: UserProfile? = null
    private var authToken: String = ""

    fun handleUserNameInput(value: String) {
        this.userName = value
        view.switchToPhoneNumberInput()
    }

    fun handlePhoneNumberInput(value: String) {
        this.phoneNumber = value

        view.showProgressBar()
        disposableOnDetach(grassrootAuthApi
                .register(phoneNumber, userName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stringRestResponse ->
                    view.closeProgressBar()
                    view.switchToOtpInput(if (BuildConfig.DEBUG) stringRestResponse.data else "")
                }) { throwable ->
                    throwable.printStackTrace()
                    view.closeProgressBar()
                    view.showErrorSnackbar(R.string.error_server_unreachable)
                })
    }

    fun handleOtpNumberInput(value: String) {
        this.otpCode = value
        view.showProgressBar()
        disposableOnDetach(grassrootAuthApi
                .verifyRegistrationCode(phoneNumber, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.storeSuccessfulAuthAndProceed(it) }) { throwable ->
                    view.closeProgressBar()
                    throwable.printStackTrace()
                })
    }

    fun finishRegistrationRequested() {
        view.finishRegistration(authToken, DashboardActivity::class.java)
    }


    private fun storeSuccessfulAuthAndProceed(response: RestResponse<TokenResponse>) {

        val tokenAndUserDetails = response.data

        val userDetails = userDetailsService.storeUserDetails(tokenAndUserDetails.userUid,
                tokenAndUserDetails.msisdn,
                tokenAndUserDetails.displayName,
                tokenAndUserDetails.systemRole,
                tokenAndUserDetails.token)

        val disposable = userDetails
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userProfile ->
                    this.userProfile = userProfile
                    this.authToken = authToken
                    view.closeProgressBar()
                    view.switchToSuccessScreen(tokenAndUserDetails.token)
                }, { it.printStackTrace() })

        disposableOnDetach(disposable)
    }


}