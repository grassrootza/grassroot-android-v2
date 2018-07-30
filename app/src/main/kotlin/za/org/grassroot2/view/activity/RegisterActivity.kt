package za.org.grassroot2.view.activity

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.util.PhoneNumberUtil
import za.org.grassroot2.presenter.RegistrationPresenter
import za.org.grassroot2.services.account.AuthConstants
import za.org.grassroot2.view.RegistrationView
import za.org.grassroot2.view.fragment.SingleTextInputFragment
import za.org.grassroot2.view.fragment.SuccessFragment
import javax.inject.Inject


class RegisterActivity : GrassrootActivity(), RegistrationView {
    override val layoutResourceId: Int
        get() = R.layout.activity_register

    private lateinit var userNameFragment: SingleTextInputFragment
    private lateinit var phoneNumberFragment: SingleTextInputFragment
    private lateinit var passwordFragment: SingleTextInputFragment
    private lateinit var otpFragment: SingleTextInputFragment
    private lateinit var successFragment: SuccessFragment

    private var debugOtp: String = ""
    private var userName: String = ""

    @Inject lateinit var presenter: RegistrationPresenter

    override fun onInject(component: ActivityComponent) = component.inject(this)

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

        passwordFragment = SingleTextInputFragment.newInstance(
                R.string.register_password_title,
                R.string.register_password_subtitle,
                R.string.register_password_label,
                R.string.register_password_hint,
                R.string.button_next)

        otpFragment = SingleTextInputFragment.newInstance(
                R.string.register_otp_title,
                R.string.register_otp_subtitle,
                R.string.register_otp_label,
                R.string.register_otp_hint,
                R.string.button_finish)


        disposables.add(userNameFragment.viewCreated().subscribe({ integer ->

            userNameFragment.toggleBackOtherButton(true)
            userNameFragment.toggleNextDoneButton(false)
            userNameFragment.setInputType(InputType.TYPE_CLASS_TEXT)
            userNameFragment.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            userNameFragment.focusOnInput()
        }, { throwable -> Timber.e(throwable) }))


        disposables.add(phoneNumberFragment.viewCreated().subscribe({ integer ->
            disposables.add(phoneNumberFragment.textInputChanged().subscribe({ currentInput ->
                phoneNumberFragment.toggleNextDoneButton(PhoneNumberUtil.isPossibleNumber(currentInput))
            }))
            phoneNumberFragment.toggleBackOtherButton(true)
            phoneNumberFragment.toggleNextDoneButton(false)
            phoneNumberFragment.setInputType(InputType.TYPE_CLASS_PHONE)
            phoneNumberFragment.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            phoneNumberFragment.focusOnInput()
        }, { throwable -> }))


        disposables.add(passwordFragment.viewCreated().subscribe({ integer ->

            passwordFragment.toggleBackOtherButton(true)
            passwordFragment.toggleNextDoneButton(false)
            passwordFragment.setInputType(TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD)
            passwordFragment.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            passwordFragment.focusOnInput()
        }, { throwable -> }))


        disposables.add(otpFragment.viewCreated().subscribe({ integer ->
            otpFragment.toggleBackOtherButton(false)
            otpFragment.toggleNextDoneButton(false)
            otpFragment.setInputType(InputType.TYPE_CLASS_NUMBER)
            otpFragment.setImeOptions(EditorInfo.IME_ACTION_DONE)
            otpFragment.focusOnInput()
            if (!TextUtils.isEmpty(debugOtp)) {
                otpFragment.setInputDefault(debugOtp)
            }
        }, { throwable -> Timber.e(throwable) }))


        successFragment = SuccessFragment()

        disposables.add(successFragment.viewCreated().subscribe({ integer ->
            successFragment.setText(
                    getString(R.string.register_success_title),
                    getString(R.string.register_success_subtitle, this.userName),
                    getString(R.string.button_start)
            )
        }, { throwable -> Timber.e(throwable) }))



        swichToStep(userNameFragment)

    }

    override fun switchToUserNameInput() = swichToStep(userNameFragment)


    override fun switchToPhoneNumberInput() = swichToStep(phoneNumberFragment)


    override fun switchToPasswordInput() = swichToStep(passwordFragment)


    override fun switchToOtpInput(otpValue: String) {
        this.debugOtp = otpValue
        swichToStep(otpFragment)
    }

    override fun switchToSuccessScreen(userName: String) {
        this.userName = userName
        this.swichToStep(successFragment)
        hideKeyboard() // on some devices it's not hidden automatically even if imeAction is DONE
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
                    this.passwordFragment -> presenter.handlePasswordInput(e.value.toString())
                    this.otpFragment -> presenter.handleOtpNumberInput(e.value.toString())
                    this.successFragment -> presenter.finishRegistrationRequested()
                }
            }
            SingleTextInputFragment.SingleInputTextEventType.BACK -> {
                when (e.source) {
                    this.phoneNumberFragment -> swichToStep(userNameFragment)
                    this.passwordFragment -> swichToStep(phoneNumberFragment)
                    this.otpFragment -> swichToStep(phoneNumberFragment)
                }
            }
        }
    }



}