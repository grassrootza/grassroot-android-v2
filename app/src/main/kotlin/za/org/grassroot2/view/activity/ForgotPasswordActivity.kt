package za.org.grassroot2.view.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.util.PhoneNumberUtil
import za.org.grassroot2.presenter.ForgottenPasswordPresenter
import za.org.grassroot2.view.ForgottenPasswordView
import za.org.grassroot2.view.fragment.SingleTextInputFragment
import za.org.grassroot2.view.fragment.SuccessFragment
import javax.inject.Inject

class ForgotPasswordActivity : GrassrootActivity(), ForgottenPasswordView {
    override val layoutResourceId: Int
        get() = R.layout.activity_register

    private lateinit var phoneNumberFragment: SingleTextInputFragment
    private lateinit var passwordFragment: SingleTextInputFragment
    private lateinit var otpFragment: SingleTextInputFragment
    private lateinit var successFragment: SuccessFragment

    private var debugOtp = ""
    private var debugPassword = ""

    @Inject lateinit var presenter: ForgottenPasswordPresenter

    override fun onInject(component: ActivityComponent) = component.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        presenter.attach(this)

        if (loggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }


        phoneNumberFragment = SingleTextInputFragment.newInstance(
                R.string.reset_password_number_title,
                R.string.reset_password_number_subtitle,
                R.string.reset_password_number_label,
                R.string.reset_password_number_hint,
                R.string.button_next)


        passwordFragment = SingleTextInputFragment.newInstance(
                R.string.reset_password_password_title,
                R.string.reset_password_password_subtitle,
                R.string.reset_password_password_label,
                R.string.reset_password_password_hint,
                R.string.button_next)

        otpFragment = SingleTextInputFragment.newInstance(
                R.string.reset_password_otp_title,
                R.string.reset_password_otp_subtitle,
                R.string.reset_password_otp_label,
                R.string.reset_password_otp_hint,
                R.string.button_finish)



        disposables.add(phoneNumberFragment.viewCreated().subscribe({ integer ->
            disposables.add(phoneNumberFragment.textInputChanged().subscribe { currentInput ->
                phoneNumberFragment.toggleNextDoneButton(PhoneNumberUtil.isPossibleNumber(currentInput))
            })
            phoneNumberFragment.toggleBackOtherButton(true)
            phoneNumberFragment.toggleNextDoneButton(false)
            phoneNumberFragment.setInputType(InputType.TYPE_CLASS_PHONE)
            phoneNumberFragment.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            phoneNumberFragment.focusOnInput()
        }, { throwable -> }))


        disposables.add(passwordFragment.viewCreated().subscribe({ integer ->

            passwordFragment.toggleBackOtherButton(true)
            passwordFragment.toggleNextDoneButton(false)
            passwordFragment.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
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
                    getString(R.string.password_change_success_title),
                    getString(R.string.password_change_success_subtitle),
                    getString(R.string.password_change_success_button)
            )
        }, { throwable -> Timber.e(throwable) }))

        swichToStep(phoneNumberFragment)
    }

    override fun switchToPhoneNumberInput() {
        this.swichToStep(phoneNumberFragment)
    }

    override fun switchToOtpInput(otpValue: String) {
        this.debugOtp = otpValue
        swichToStep(otpFragment)
    }

    override fun switchToPasswordInput() {
        this.swichToStep(passwordFragment)
    }

    override fun passwordChangeSuccess(debugPassword: String) {
        this.debugPassword = debugPassword
        this.swichToStep(successFragment)
        hideKeyboard()

    }

    private fun navigateLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("newPassword", this.debugPassword)
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
                    this.phoneNumberFragment -> presenter.handlePhoneNumberInput(e.value.toString())
                    this.passwordFragment -> presenter.handlePasswordInput(e.value.toString())
                    this.otpFragment -> presenter.handleOtpInput(e.value.toString())
                    this.successFragment -> navigateLoginScreen()
                }
            }
            SingleTextInputFragment.SingleInputTextEventType.BACK -> {
                when (e.source) {
                    this.passwordFragment -> swichToStep(phoneNumberFragment)
                }
            }
        }
    }


}