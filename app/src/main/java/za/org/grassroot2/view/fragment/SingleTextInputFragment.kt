package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_single_text_input.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.rxbinding.RxViewUtils
import za.org.grassroot2.view.SingleInputNextOtherView

class SingleTextInputFragment : TextInputFragment(), SingleInputNextOtherView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_single_text_input

    private var nextBtnRes: Int = 0
    private var inputLabelRes: Int = 0
    private var inputHintRes: Int = 0

    override fun getInputText(): TextView = inputText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            val args = arguments
            nextBtnRes = args!!.getInt(NEXT_BUTTON_RES)
            inputLabelRes = args.getInt(INPUT_LABEL_RES)
            inputHintRes = args.getInt(INPUT_HINT_RES)
        }
    }

    override fun onInject(activityComponent: ActivityComponent) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        header.setText(headerTextRes)
        explanation!!.setText(explanTextRes)

        nextButton.setText(nextBtnRes)
        inputLabel.setText(inputLabelRes)
        inputText.setHint(inputHintRes)

        lifecyclePublisher.onNext(GrassrootFragment.ACTION_FRAGMENT_VIEW_CREATED)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val editTextNext = RxTextView
                .editorActions(inputText, RxViewUtils.imeNextDonePredicate())
                .filter { integer ->
                    inputText != null // because Android
                }
                .map<CharSequence> { integer -> inputText.text }
        val nextButtonClicked = RxView
                .clicks(nextButton!!).map<CharSequence> { o -> inputText.text }

        val backClick = RxView.clicks(backButton!!)
                .map<CharSequence> { o -> inputText.text }

        disposables.add(RxTextView.textChanges(inputText).subscribe({ charSequence ->
            if (charSequence.length > 1) {
                toggleNextDoneButton(true)
            } }, { Timber.e(it) }))

        disposables.add(Observable.merge(editTextNext, nextButtonClicked)
                .subscribe({ charSequence ->
                    EventBus.getDefault().post(SingleInputTextEvent(this, SingleInputTextEventType.DONE, charSequence))
                }, { it.printStackTrace() }))

        disposables.add(backClick.subscribe({ charSequence ->
                    EventBus.getDefault().post(SingleInputTextEvent(this, SingleInputTextEventType.BACK, "")) }, { Timber.e(it) }))
    }

    override fun toggleNextDoneButton(enabled: Boolean) {
        nextButton!!.isEnabled = enabled
    }

    override fun toggleBackOtherButton(enabled: Boolean) {
        backButton!!.isEnabled = enabled
        backButton!!.visibility = if (enabled) View.VISIBLE else View.INVISIBLE
    }


    enum class SingleInputTextEventType {
        CHANGE, DONE, BACK
    }

    class SingleInputTextEvent(val source: GrassrootFragment, val type: SingleInputTextEventType, val value: CharSequence)

    companion object {

        private val INPUT_LABEL_RES = "INPUT_LABEL_RES"
        private val INPUT_HINT_RES = "INPUT_HINT_RES"
        private val NEXT_BUTTON_RES = "NEXT_BUTTON_RES"

        fun newInstance(headerTextRes: Int, explanTextRes: Int, inputLabelRes: Int, inputHintRes: Int, nextButtonRes: Int): SingleTextInputFragment {
            val fragment = SingleTextInputFragment()
            val args = Bundle()
            TextInputFragment.addStandardArgs(args, headerTextRes, explanTextRes)
            args.putInt(INPUT_LABEL_RES, inputLabelRes)
            args.putInt(INPUT_HINT_RES, inputHintRes)
            args.putInt(NEXT_BUTTON_RES, nextButtonRes)
            fragment.arguments = args
            return fragment
        }
    }
}