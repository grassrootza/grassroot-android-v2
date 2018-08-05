package za.org.grassroot2.view

import io.reactivex.Observable

/**
 * Created by luke on 2017/08/25.
 */

interface FragmentView : ProgressBarContainer {
    fun viewCreated(): Observable<Int>
    fun showNoConnectionMessage()

    fun showErrorDialog(errorMsgResId: Int)
    fun handleNoConnection()
    fun handleNoConnectionUpload()
}
