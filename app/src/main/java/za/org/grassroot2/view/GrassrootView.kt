package za.org.grassroot2.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import za.org.grassroot.messaging.dto.MessageDTO

interface GrassrootView : ProgressBarContainer {

    val activity: Activity

    fun showMessageDialog(text: String)
    fun showSuccessSnackbar(successMsg: Int)
    fun showErrorSnackbar(errorTextRes: Int)
    fun showSuccessDialog(textRes: Int, okayListener: View.OnClickListener)
    fun showErrorDialog(errorMsg: Int)

    fun closeKeyboard()
    fun launchActivity(cls: Class<*>, args: Bundle)

    fun handleNoConnection()
    fun handleNoConnectionUpload()
    fun showNoConnectionMessage()

    fun sendCGMMessage(messageDTO: MessageDTO)

    fun hideKeyboard()
}