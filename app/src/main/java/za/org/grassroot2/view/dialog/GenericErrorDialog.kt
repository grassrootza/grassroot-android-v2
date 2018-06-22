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
import kotlinx.android.synthetic.main.dialog_generic_error.*

import za.org.grassroot2.R

class GenericErrorDialog : DialogFragment() {

    private val errorDialog: Dialog
        get() {
            val builder = activity?.let { AlertDialog.Builder(it) }
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_generic_error, null, false)
            val errorMsgResID = arguments?.getInt(MSG_RES_ID_ARG)
            errorMsgResID?.let { title.setText(it) }
            close.setOnClickListener { v1 -> dismiss() }
            done.setOnClickListener { v1 -> dismiss() }
            builder?.setView(v)
            val d = builder?.create()
            d?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return errorDialog
    }

    companion object {

        private val MSG_RES_ID_ARG = "MSG_RES_ID_ARG"

        fun newInstance(dialogType: Int): DialogFragment {
            val dialog = GenericErrorDialog()
            val args = Bundle()
            args.putInt(MSG_RES_ID_ARG, dialogType)
            dialog.arguments = args
            return dialog
        }
    }


}
