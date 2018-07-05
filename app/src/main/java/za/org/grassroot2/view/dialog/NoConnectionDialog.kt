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

import za.org.grassroot2.R

class NoConnectionDialog : DialogFragment() {
    private var dialogType: Int = 0

    private val notAuthorizedDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity!!)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_no_connection_not_authorized, null, false)
            v.findViewById<View>(R.id.close).setOnClickListener { v1 -> dismiss() }
            v.findViewById<View>(R.id.done).setOnClickListener { v1 -> dismiss() }
            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    private val authorizedDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity!!)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_no_connection_authorized, null, false)
            v.findViewById<View>(R.id.continueButton).setOnClickListener { v1 -> dismiss() }
            v.findViewById<View>(R.id.retryButton).setOnClickListener { v1 -> dismiss() }
            v.findViewById<View>(R.id.close).setOnClickListener { v1 -> dismiss() }
            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogType = arguments!!.getInt(EXTRA_TYPE)
        when (dialogType) {
            TYPE_NOT_AUTHORIZED -> return notAuthorizedDialog
            else -> return authorizedDialog
        }
    }

    companion object {

        val TYPE_NOT_AUTHORIZED = 0
        val TYPE_AUTHORIZED = 1
        private val EXTRA_TYPE = "type"

        fun newInstance(dialogType: Int): DialogFragment {
            val dialog = NoConnectionDialog()
            val args = Bundle()
            args.putInt(EXTRA_TYPE, dialogType)
            dialog.arguments = args
            return dialog
        }
    }
}
