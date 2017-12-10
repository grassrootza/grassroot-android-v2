package za.org.grassroot2.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import za.org.grassroot2.R

class GenericSuccessDialog : DialogFragment() {

    lateinit var okayListener: View.OnClickListener

    private val successDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_generic_success, null, false)
            // note: use these instead of synthetic extensions as need to be finding within view returned from inflater
            (v.findViewById(R.id.title) as TextView).setText(arguments.getInt(TITLE_RES_ID_ARG))
            v.findViewById(R.id.okButton).setOnClickListener(okayListener)
            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return successDialog
    }

    companion object {

        private val TITLE_RES_ID_ARG = "TITLE_RES_ID_ARG"

        // todo: once migrate GrassrootActivity to Kotlin (one of only complex migrations, maybe), switch to operator
        @JvmStatic fun newInstance(titleRes: Int, okListener: View.OnClickListener): DialogFragment {
            val dialog = GenericSuccessDialog()
            val args = Bundle()
            args.putInt(TITLE_RES_ID_ARG, titleRes)
            dialog.okayListener = okListener
            dialog.arguments = args
            return dialog
        }
    }


}
