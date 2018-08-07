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

class GenericMessageDialog : DialogFragment() {

    private val msgDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity!!)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_generic_message, null, false)
            val text = arguments!!.getString(MSG_BODY)
            (v.findViewById<View>(R.id.msgBody) as TextView).text = text
            v.findViewById<View>(R.id.close).setOnClickListener { v1 -> dismiss() }
            v.findViewById<View>(R.id.done).setOnClickListener { v1 -> dismiss() }
            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return msgDialog
    }

    companion object {

        private val MSG_BODY = "MSG_BODY"

        fun newInstance(text: String): DialogFragment {
            val dialog = GenericMessageDialog()
            val args = Bundle()
            args.putString(MSG_BODY, text)
            dialog.arguments = args
            return dialog
        }
    }


}
