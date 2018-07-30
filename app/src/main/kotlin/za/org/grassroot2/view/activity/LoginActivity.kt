package za.org.grassroot2.view.activity

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.LoginPresenter
import za.org.grassroot2.services.account.AuthConstants
import za.org.grassroot2.view.LoginView
import javax.inject.Inject

class LoginActivity : GrassrootActivity(), LoginView {
    override val layoutResourceId: Int
        get() = R.layout.activity_login

    @Inject lateinit var presenter: LoginPresenter

    private lateinit var phoneNumberChangeObserver: Disposable

    override fun onInject(component: ActivityComponent) = component.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        presenter.attach(this)

        toggleSubmitButton(false)

        val o = RxTextView.textChanges(phoneNumberTf)
        this.phoneNumberChangeObserver = o.subscribe(
                { presenter.validatePhoneNumber(it) },
                { Timber.d(it) }
        )

        val changecPassword = intent.getStringExtra("newPassword")
        if (changecPassword != null) {
            passwordTf.setText(changecPassword)
        }

        loginBtn.setOnClickListener {
            val phoneNumber = phoneNumberTf.text.toString()
            val password = passwordTf.text.toString()
            presenter.login(phoneNumber, password)
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            finish()
            startActivity(intent)
        }

    }

    override fun toggleSubmitButton(enabled: Boolean) {
        loginBtn.isEnabled = enabled
    }

    override fun loginSuccessContinue(@NonNull authToken: String, @NonNull nextActivity: Class<*>) {
        val intent = Intent(this, nextActivity)
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, AuthConstants.ACCOUNT_NAME)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AuthConstants.ACCOUNT_TYPE)
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
        setAccountAuthenticatorResult(intent.extras)
        setResult(Activity.RESULT_OK, intent)
        finish()
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
        phoneNumberChangeObserver.dispose()
    }
}