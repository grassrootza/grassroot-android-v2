package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import za.org.grassroot2.view.SingleTextView

/**
 * Created by luke on 2017/08/10.
 */

abstract class TextInputFragment : GrassrootFragment(), SingleTextView {

    protected var headerTextRes: Int = 0
    protected var explanTextRes: Int = 0

    abstract fun getInputText(): TextView?;

    override val inputValue: String
        get() = getInputText()!!.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            val args = arguments
            headerTextRes = args!!.getInt(HEADER_TEXT_RES)
            explanTextRes = args.getInt(EXPLAN_TEXT_RES)
        }
    }

    override fun textInputChanged(): Observable<CharSequence> {
        return RxTextView.textChanges(getInputText()!!)
    }

    override fun setInputDefault(defaultValue: CharSequence) {
        getInputText()?.text = defaultValue
    }

    override fun displayErrorMessage(messageRes: Int) {
        getInputText()?.error = getString(messageRes)
    }

    override fun setInputType(type: Int) {
        getInputText()?.inputType = type
    }

    override fun setImeOptions(imeOptions: Int) {
        getInputText()?.imeOptions = imeOptions
    }

    override fun focusOnInput() {
        getInputText()?.requestFocus()
    }

    companion object {

        const val HEADER_TEXT_RES = "HEADER_TEXT_RES"
        const val EXPLAN_TEXT_RES = "EXPLAN_TEXT_RES"

        fun addStandardArgs(args: Bundle, headerTextRes: Int, explanTextRes: Int) {
            args.putInt(HEADER_TEXT_RES, headerTextRes)
            args.putInt(EXPLAN_TEXT_RES, explanTextRes)
        }
    }

}
