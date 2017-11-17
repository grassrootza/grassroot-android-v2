package za.org.grassroot2.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.R
import za.org.grassroot2.presenter.activity.BasePresenter
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootAuthApi
import za.org.grassroot2.view.ForgottenPasswordView
import javax.inject.Inject

class ForgottenPasswordPresenter @Inject constructor(val grassrootAuthApi: GrassrootAuthApi,
                                                     val userDetailsService: UserDetailsService) : BasePresenter<ForgottenPasswordView>() {

    private var phoneNumber: String = ""
    private var newPassword: String = ""


    fun handlePhoneNumberInput(value: String) {

        if (value.isNotEmpty() && value.length >= 6) {
            this.phoneNumber = value
            view.switchToPasswordInput()

        } else {
            view.hideKeyboard()
            view.showErrorSnackbar(R.string.reset_password_number_error)
        }
    }

    fun handlePasswordInput(value: String) {

        if (value.isNotEmpty() && value.length >= 6) {
            this.newPassword = value //store password to variable and get otp
            view.showProgressBar()
            disposableOnDetach(grassrootAuthApi
                    .resetPasswordRequest(phoneNumber)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ stringRestResponse ->
                        view.closeProgressBar()
                        view.switchToOtpInput(if (BuildConfig.DEBUG) stringRestResponse.data else "")
                    }) { throwable ->
                        throwable.printStackTrace()
                        view.closeProgressBar()
                        view.hideKeyboard()
                        view.showErrorSnackbar(R.string.reset_password_request_failed)
                    })
        } else {
            view.hideKeyboard()
            view.showErrorSnackbar(R.string.reset_password_password_error)
        }
    }


    fun handleOtpInput(value: String) {

        if (value.isNotEmpty() && value.length >= 6) {
            view.showProgressBar()
            disposableOnDetach(grassrootAuthApi
                    .resetPasswordConfirm(phoneNumber, newPassword, value)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ stringRestResponse ->
                        view.closeProgressBar()
                        view.passwordChangeSuccess(if (BuildConfig.DEBUG) this.newPassword else "")
                    }) { throwable ->
                        throwable.printStackTrace()
                        view.closeProgressBar()
                        view.hideKeyboard()
                        view.showErrorSnackbar(R.string.reset_password_otp_verification_failed)
                    })
        } else {
            view.hideKeyboard()
            view.showErrorSnackbar(R.string.reset_password_otp_error)
        }
    }


}