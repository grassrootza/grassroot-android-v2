package za.org.grassroot2.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.ArrayAdapter
import butterknife.OnClick
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_add_vote_responses_single_input.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import java.util.*
import java.util.concurrent.TimeUnit

class VoteOptionsSingleInputFragment : GrassrootFragment() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_add_vote_responses_single_input

    private var chosenOptionsList: MutableList<String> = ArrayList()

    private val chosenOptions = PublishSubject.create<List<String>>()
    private var listener: BackNavigationListener? = null
    var isMultiLine: Boolean = false

    fun inputAdded(): Observable<List<String>> = chosenOptions

    override fun onInject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener?
    }

    @OnClick(R.id.back)
    internal fun back() {
        listener!!.backPressed()
    }

    @OnClick(R.id.cancel)
    internal fun close() {
        activity!!.finish()
    }

    @OnClick(R.id.add)
    internal fun add() {
        val voteOptionInput = inputContainer!!.editText!!.text.toString()
        chosenOptionsList.add(voteOptionInput)
        val arrayAdapter = ArrayAdapter(context!!, R.layout.simple_single_item, chosenOptionsList)
        listView!!.adapter = arrayAdapter
        inputContainer!!.editText!!.setText("")
        arrayAdapter.notifyDataSetChanged()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inputContainer!!.hint = getString(arguments!!.getInt(EXTRA_HINT_RES))
        setMultilineIfRequired()
        title!!.setText(arguments!!.getInt(EXTRA_TITLE_RES))
        disposables.add(RxTextView.textChanges(input!!).debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ next!!.isEnabled = true }, { Timber.e(it) }))

        RxView.clicks(next!!).observeOn(AndroidSchedulers.mainThread())
                .map<List<String>> { clickEvent -> chosenOptionsList }.subscribe(chosenOptions)
    }

    private fun setMultilineIfRequired() {
        if (isMultiLine) {
            input!!.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            input!!.setSingleLine(false)
            input!!.gravity = Gravity.START or Gravity.TOP
            input!!.maxLines = 5
            input!!.minLines = 5
        }
    }

    companion object {

        private const val EXTRA_TITLE_RES = "title_res"
        private const val EXTRA_HINT_RES = "hint_res"
        private const val EXTRA_CAN_SKIP = "can_skip"

        fun newInstance(resTitle: Int, resHint: Int, canSkip: Boolean): VoteOptionsSingleInputFragment {
            val f = VoteOptionsSingleInputFragment()
            val b = Bundle()
            b.putInt(EXTRA_TITLE_RES, resTitle)
            b.putInt(EXTRA_HINT_RES, resHint)
            b.putBoolean(EXTRA_CAN_SKIP, canSkip)
            f.arguments = b
            return f
        }
    }
}

