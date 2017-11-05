package za.org.grassroot2.view.activity

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.RegistrationPresenter
import za.org.grassroot2.services.account.AuthConstants
import za.org.grassroot2.view.RegistrationView
import za.org.grassroot2.view.fragment.RegistrationSuccessFragment
import za.org.grassroot2.view.fragment.SingleTextInputFragment
import javax.inject.Inject

class RegisterActivity : GrassrootActivity(), RegistrationView {

    private lateinit var userNameFragment: SingleTextInputFragment
    private lateinit var phoneNumberFragment: SingleTextInputFragment
    private lateinit var otpFragment: SingleTextInputFragment
    private lateinit var successFragment: RegistrationSuccessFragment

    private var debugOtp: String = ""
    private var userName: String = ""

    @Inject
    lateinit var presenter: RegistrationPresenter

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_register
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        presenter.attach(this)

        if (loggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        userNameFragment = SingleTextInputFragment.newInstance(
                R.string.register_username_title,
                R.string.register_username_subtitle,
                R.string.register_username_label,
                R.string.register_username_hint,
                R.string.button_next)


        phoneNumberFragment = SingleTextInputFragment.newInstance(
                R.string.register_number_title,
                R.string.register_number_subtitle,
                R.string.register_number_label,
                R.string.register_number_hint,
                R.string.button_next)

        otpFragment = SingleTextInputFragment.newInstance(
                R.string.register_otp_title,
                R.string.register_otp_subtitle,
                R.string.register_otp_label,
                R.string.register_otp_hint,
                R.string.button_finish)

        disposables.add(otpFragment.viewCreated().subscribe({ integer ->
            otpFragment.toggleNextDoneButton(false)
            otpFragment.toggleBackOtherButton(false)
            if (!TextUtils.isEmpty(debugOtp)) {
                otpFragment.setInputDefault(debugOtp)
            }
        }, { throwable -> Timber.e(throwable) }))


        successFragment = RegistrationSuccessFragment()

        disposables.add(successFragment.viewCreated().subscribe({ integer ->
            successFragment.setMessage(getString(R.string.register_success_subtitle, this.userName))
        }, { throwable -> Timber.e(throwable) }))



        swichToStep(userNameFragment)

    }

    override fun switchToUserNameInput() {
        swichToStep(userNameFragment)
    }

    override fun switchToPhoneNumberInput() {
        swichToStep(phoneNumberFragment)
    }

    override fun switchToOtpInput(otpValue: String) {
        this.debugOtp = otpValue
        swichToStep(otpFragment)
    }

    override fun switchToSuccessScreen(userName: String) {
        this.userName = userName
        this.swichToStep(successFragment)
    }

    override fun finishRegistration(authToken: String, nextActivity: Class<*>) {
        val intent = Intent(this, nextActivity)
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, AuthConstants.ACCOUNT_NAME)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AuthConstants.ACCOUNT_TYPE)
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
        setAccountAuthenticatorResult(intent.extras)
        setResult(Activity.RESULT_OK, intent)
        finish()
        startActivity(intent)
    }


    private fun swichToStep(fragmentToShow: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.login_frag_holder, fragmentToShow)
                .commit()

    }


    @Subscribe
    fun singleInput(e: SingleTextInputFragment.SingleInputTextEvent) {

        Timber.d("Event received ${e.value}")
        when (e.type) {
            SingleTextInputFragment.SingleInputTextEventType.DONE -> {
                when (e.source) {
                    this.userNameFragment -> presenter.handleUserNameInput(e.value.toString())
                    this.phoneNumberFragment -> presenter.handlePhoneNumberInput(e.value.toString())
                    this.otpFragment -> presenter.handleOtpNumberInput(e.value.toString())
                    this.successFragment -> presenter.finishRegistrationRequested()
                }
            }
            SingleTextInputFragment.SingleInputTextEventType.BACK -> {
                when (e.source) {
                    this.userNameFragment -> handleUserNameFragmentBack()
                    this.phoneNumberFragment -> handlePhoneNumberFragmentBack()
                    this.otpFragment -> handleOtpFragmentBack()
                }
            }
        }
    }


    private fun handleUserNameFragmentBack() {

    }

    private fun handlePhoneNumberFragmentBack() {
        swichToStep(userNameFragment)
    }

    private fun handleOtpFragmentBack() {
        swichToStep(phoneNumberFragment)
    }


}