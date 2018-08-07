package za.org.grassroot2.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import butterknife.OnClick
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_meeting_single_input.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import java.util.concurrent.TimeUnit

class ActionSingleInputFragment : GrassrootFragment() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_meeting_single_input

    private val actionSubject = PublishSubject.create<String>()
    private var listener: BackNavigationListener? = null
    var isMultiLine: Boolean = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener?
    }

    @OnClick(R.id.backNav)
    internal fun back() {
        listener!!.backPressed()
    }

    @OnClick(R.id.cancel)
    internal fun close() {
        activity!!.finish()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inputContainer.hint = getString(arguments!!.getInt(EXTRA_HINT_RES))
        setMultilineIfRequired()
        header.setText(arguments!!.getInt(EXTRA_TITLE_RES))
        item_description.setText(arguments!!.getInt(EXTRA_DESC_RES))
        if (arguments!!.getBoolean(EXTRA_CAN_SKIP, false)) {
            cancel.setText(R.string.button_skip)
            cancel.setOnClickListener { v -> actionSubject.onNext("") }
        }
        disposables.add(RxTextView.textChanges(input!!).debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ charSequence -> next!!.isEnabled = charSequence.length > 3 }, { it.printStackTrace() }))

        RxView.clicks(next!!).map { o -> input!!.text.toString() }.subscribe(actionSubject)

        RxTextView.editorActionEvents(input!!) { textViewEditorActionEvent -> textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_DONE && input!!.length() > 3 }
                .map { _ -> input!!.text.toString() }.subscribe(actionSubject)
    }

    private fun setMultilineIfRequired() {
        if (isMultiLine) {
            input!!.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            input!!.setSingleLine(false)
            input!!.gravity = Gravity.LEFT or Gravity.TOP
            input!!.maxLines = 5
            input!!.minLines = 5
        }
    }

    fun inputAdded(): Observable<String> = actionSubject

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    companion object {

        private val EXTRA_TITLE_RES = "title_res"
        private val EXTRA_DESC_RES = "desc_res"
        private val EXTRA_HINT_RES = "hint_res"
        private val EXTRA_CAN_SKIP = "can_skip"

        fun newInstance(resTitle: Int, resDesc: Int, resHint: Int, canSkip: Boolean): ActionSingleInputFragment {
            val f = ActionSingleInputFragment()
            val b = Bundle()
            b.putInt(EXTRA_TITLE_RES, resTitle)
            b.putInt(EXTRA_DESC_RES, resDesc)
            b.putInt(EXTRA_HINT_RES, resHint)
            b.putBoolean(EXTRA_CAN_SKIP, canSkip)
            f.arguments = b
            return f
        }
    }
}
