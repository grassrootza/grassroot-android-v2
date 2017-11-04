package za.org.grassroot2.presenter

import android.content.Intent
import za.org.grassroot2.view.WelcomeView
import za.org.grassroot2.view.activity.LoginActivity2
import javax.inject.Inject


class WelcomePresenter @Inject constructor() : BasePresenter<WelcomeView>() {


    fun navigateToLoginScreen() {
        val next = Intent(view.activity, LoginActivity2::class.java)
        view.activity.startActivity(next)
    }

    fun navigateToCreateAccountScreen() {

    }
}