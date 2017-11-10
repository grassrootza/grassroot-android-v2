package za.org.grassroot2.view.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.util.PhoneNumberUtil
import za.org.grassroot2.presenter.ForgottenPasswordPresenter
import za.org.grassroot2.view.ForgottenPasswordView
import za.org.grassroot2.view.fragment.SingleTextInputFragment
import javax.inject.Inject

class ForgotPasswordActivity : GrassrootActivity(), ForgottenPasswordView {

    private lateinit var phoneNumberFragment: SingleTextInputFragment


    @Inject
    lateinit var presenter: ForgottenPasswordPresenter

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


        phoneNumberFragment = SingleTextInputFragment.newInstance(
                R.string.forgotten_password_title,
                R.string.forgotten_password_subtitle,
                R.string.forgotten_password_label,
                R.string.forgotten_password_hint,
                R.string.ok)



        disposables.add(phoneNumberFragment.viewCreated().subscribe({ integer ->
            disposables.add(phoneNumberFragment.textInputChanged().subscribe({ currentInput ->
                phoneNumberFragment.toggleNextDoneButton(PhoneNumberUtil.isPossibleNumber(currentInput))
            }))
            phoneNumberFragment.toggleBackOtherButton(true)
            phoneNumberFragment.toggleNextDoneButton(false)
            phoneNumberFragment.setInputType(InputType.TYPE_CLASS_PHONE)
            phoneNumberFragment.setImeOptions(EditorInfo.IME_ACTION_DONE)
            phoneNumberFragment.focusOnInput()
        }, { throwable -> }))


        swichToStep(phoneNumberFragment)

    }


    override fun passwordChangeSuccess(password: String, nextActivity: Class<*>) {
        val intent = Intent(this, nextActivity)
        intent.putExtra("newPassword", password)
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
                }
            }
        }
    }


}