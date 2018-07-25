package za.org.grassroot2.view.fragment


import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.fragment_long_text_input.*
import org.greenrobot.eventbus.EventBus
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.view.SingleInputNextOtherView

class LongTextInputFragment : TextInputFragment(), SingleInputNextOtherView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_long_text_input

    private var inputHintRes: Int = 0
    private var skipBtnRes: Int = 0
    private var nextBtnRes: Int = 0

    override fun getInputText(): TextView = inputText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            val args = arguments
            skipBtnRes = args!!.getInt(SKIP_BUTTON_RES)
            nextBtnRes = args.getInt(NEXT_BUTTON_RES)
            inputHintRes = args.getInt(INPUT_HINT_RES)
        }
        lifecyclePublisher.onNext(GrassrootFragment.ACTION_FRAGMENT_CREATED)
    }

    override fun onInject(activityComponent: ActivityComponent) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        header.setText(headerTextRes) // res value set via super calls
        inputText.setHint(inputHintRes)
        nextButton!!.setText(nextBtnRes)
        skipButton!!.setText(skipBtnRes)
        lifecyclePublisher.onNext(GrassrootFragment.ACTION_FRAGMENT_VIEW_CREATED)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        disposables.add(RxView.clicks(skipButton!!).subscribe { o -> EventBus.getDefault().post(LongInputEvent(null)) })
        disposables.add(RxView.clicks(nextButton!!).map<Editable> { o -> inputText.text }
                .subscribe { editable -> EventBus.getDefault().post(LongInputEvent(editable.toString())) })
    }

    override fun toggleNextDoneButton(enabled: Boolean) {
        nextButton!!.isEnabled = enabled
    }

    override fun toggleBackOtherButton(enabled: Boolean) {
        skipButton!!.isEnabled = enabled
    }

    class LongInputEvent(val s: String?)

    companion object {

        private const val INPUT_HINT_RES = "INPUT_HINT_RES"
        private const val SKIP_BUTTON_RES = "SKIP_BUTTON_RES"
        private const val NEXT_BUTTON_RES = "NEXT_BUTTON_RES"

        fun newInstance(headerRes: Int, textHintRes: Int,
                        skipButtonRes: Int, nextButtonRes: Int): LongTextInputFragment {
            val fragment = LongTextInputFragment()
            val args = Bundle()
            args.putInt(TextInputFragment.HEADER_TEXT_RES, headerRes)
            args.putInt(INPUT_HINT_RES, textHintRes)
            args.putInt(SKIP_BUTTON_RES, skipButtonRes)
            args.putInt(NEXT_BUTTON_RES, nextButtonRes)
            fragment.arguments = args
            return fragment
        }
    }
}
