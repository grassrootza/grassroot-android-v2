package za.org.grassroot2.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_generic_select.*
import za.org.grassroot2.R
import java.util.*

class GenericSelectDialog : DialogFragment() {
    private var listeners = ArrayList<View.OnClickListener>()

    // note: may need to switch these back to v.findByViewId from synthetic extensions (see generic success dialog)
    private val pickDialog: Dialog
        get() {
            val builder = activity?.let { AlertDialog.Builder(it) }
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_generic_select, null, false)
            val b = arguments
            b?.getString(TITLE_TEXTS_ID_ARG)?.let {
                val title = v.findViewById(R.id.selectTitle) as TextView
                title.visibility = View.VISIBLE
                title.text = b.getString(TITLE_TEXTS_ID_ARG)
                titleSeparator.visibility = View.VISIBLE
            }
            val buttonTextResources = b?.getIntArray(BTN_TEXTS_ID_ARG)
            buttonOne.setText(buttonTextResources!![0])
            buttonOne.setOnClickListener(listeners[0])
            buttonTwo.setText(buttonTextResources[1])
            buttonTwo.setOnClickListener(listeners[1])
            if (buttonTextResources.size == 3) {
                val btnThree = v.findViewById(R.id.buttonThree) as Button
                btnThree.visibility = View.VISIBLE
                btnThree.setText(buttonTextResources[2])
                btnThree.setOnClickListener(listeners[2])
                buttonsTwoThreeSeparator.visibility = View.VISIBLE
            }
            close.setOnClickListener { v1 -> dismiss() }
            builder?.setView(v)
            val d = builder?.create()
            d?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    fun setListeners(listeners: ArrayList<View.OnClickListener>) {
        this.listeners = listeners
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return pickDialog

    }

    companion object {

        private val TITLE_TEXTS_ID_ARG = "TITLE_TEXTS_ARG"
        private val BTN_TEXTS_ID_ARG = "BTN_TEXTS_ID_ARG"

        operator fun get(titleText: String, buttonTextResources: IntArray, buttonListeners: ArrayList<View.OnClickListener>): GenericSelectDialog {
            if (buttonTextResources.size != buttonListeners.size) {
                throw IllegalArgumentException("Error! Must have same number of texts as listeners")
            }
            if (buttonTextResources.size < 2) {
                throw IllegalArgumentException("Error! Must have at least two buttons")
            }
            val dialog = GenericSelectDialog()
            val args = Bundle()
            if (!TextUtils.isEmpty(titleText)) {
                args.putString(TITLE_TEXTS_ID_ARG, titleText)
            }
            args.putIntArray(BTN_TEXTS_ID_ARG, buttonTextResources)
            dialog.setListeners(buttonListeners)
            dialog.arguments = args
            return dialog
        }
    }

}
