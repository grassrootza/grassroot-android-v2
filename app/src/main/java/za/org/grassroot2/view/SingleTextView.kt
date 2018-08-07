package za.org.grassroot2.view

import io.reactivex.Observable

/**
 * Created by luke on 2017/08/10.
 */
interface SingleTextView : FragmentView {

    val inputValue: String

    fun textInputChanged(): Observable<CharSequence>

    fun setInputDefault(defaultValue: CharSequence)
    fun displayErrorMessage(messageRes: Int)

    fun setInputType(type: Int)
    fun setImeOptions(imeOptions: Int)

    fun focusOnInput()
}
