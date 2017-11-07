package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_registration_success.*
import org.greenrobot.eventbus.EventBus
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.rxbinding.RxView

class RegistrationSuccessFragment : GrassrootFragment() {


    override fun onInject(activityComponent: ActivityComponent) {}

    private var successMessage = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        lifecyclePublisher.onNext(GrassrootFragment.ACTION_FRAGMENT_VIEW_CREATED)
        return v
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val nextButtonClicked = RxView.clicks(button_next)

        success_message.text = this.successMessage

        disposables.add(nextButtonClicked.subscribe(
                { EventBus.getDefault().post(SingleTextInputFragment.SingleInputTextEvent(this, SingleTextInputFragment.SingleInputTextEventType.DONE, "")) },
                { it.printStackTrace() }))

    }


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_registration_success
    }


    fun setMessage(message: String) {
        successMessage = message
    }

}
