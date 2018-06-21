package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.fragment_success.*
import org.greenrobot.eventbus.EventBus
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent

class SuccessFragment : GrassrootFragment() {


    override fun onInject(activityComponent: ActivityComponent) {}

    private var titleText = ""
    private var subtitleText = ""
    private var buttonText = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        lifecyclePublisher.onNext(GrassrootFragment.ACTION_FRAGMENT_VIEW_CREATED)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val nextButtonClicked = RxView.clicks(button_next)

        title.text = this.titleText
        subtitle.text = this.subtitleText
        button_next.text = this.buttonText

        disposables.add(nextButtonClicked.subscribe(
                { EventBus.getDefault().post(SingleTextInputFragment.SingleInputTextEvent(this, SingleTextInputFragment.SingleInputTextEventType.DONE, "")) },
                { it.printStackTrace() }))

    }


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_success
    }


    fun setText(titleText: String, subtitleText: String, buttonText: String) {
        this.titleText = titleText
        this.subtitleText = subtitleText
        this.buttonText = buttonText
    }

}
