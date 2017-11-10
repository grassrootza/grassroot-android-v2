package za.org.grassroot2.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.R
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootAuthApi
import za.org.grassroot2.view.ForgottenPasswordView
import za.org.grassroot2.view.activity.LoginActivity
import javax.inject.Inject

class ForgottenPasswordPresenter @Inject constructor(val grassrootAuthApi: GrassrootAuthApi,
                                                     val userDetailsService: UserDetailsService) : BasePresenter<ForgottenPasswordView>() {

    private var phoneNumber: String = ""


    fun handlePhoneNumberInput(value: String) {

        if (value.isNotEmpty() && value.length >= 6) {
            this.phoneNumber = value
            view.showProgressBar()
            disposableOnDetach(grassrootAuthApi
                    .forgotPassword(phoneNumber)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ stringRestResponse ->
                        view.closeProgressBar()
                        view.passwordChangeSuccess(if (BuildConfig.DEBUG) stringRestResponse.data else "", LoginActivity::class.java)
                    }) { throwable ->
                        throwable.printStackTrace()
                        view.closeProgressBar()
                        view.hideKeyboard()
                        view.showErrorSnackbar(R.string.forgotten_password_change_failed)
                    })
        } else {
            view.hideKeyboard()
            view.showErrorSnackbar(R.string.register_password_error)
        }
    }


}