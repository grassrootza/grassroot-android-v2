package za.org.grassroot2.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

import com.jakewharton.rxbinding2.widget.RxTextView

import za.org.grassroot2.R

class AddMemberDialog : DialogFragment() {

    private var dialogType: Int = 0
    private var listener: AddMemberDialogListener? = null
    private var contactListener: ContactFilledInListener? = null

    private val fillDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity!!)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_contact_manual_add, null, false)
            val name = v.findViewById<View>(R.id.nameEdittext) as EditText
            val phone = v.findViewById<View>(R.id.numberEdittext) as EditText
            val addButton = v.findViewById<View>(R.id.add)
            RxTextView.textChanges(name).subscribe({ charSequence -> addButton.isEnabled = inputValid(name, phone) }, { it.printStackTrace() })
            RxTextView.textChanges(phone).subscribe({ charSequence -> addButton.isEnabled = inputValid(name, phone) }, { it.printStackTrace() })
            v.findViewById<View>(R.id.close).setOnClickListener { v1 -> dismiss() }
            addButton.setOnClickListener { v1 ->
                contactListener!!.contact(name.text.toString(), phone.text.toString())
                dismiss()
            }
            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    private val pickDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity!!)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_contact_selection_method, null, false)
            v.findViewById<View>(R.id.contactBook).setOnClickListener { v1 ->
                dismiss()
                listener!!.contactBook()
            }
            v.findViewById<View>(R.id.manual).setOnClickListener { v1 ->
                dismiss()
                listener!!.manual()
            }
            v.findViewById<View>(R.id.close).setOnClickListener { v1 -> dismiss() }
            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    fun setAddMemberDialogListener(listener: AddMemberDialogListener) {
        this.listener = listener
    }

    fun setContactListener(contactListener: ContactFilledInListener) {
        this.contactListener = contactListener
    }

    interface AddMemberDialogListener {
        fun contactBook()
        fun manual()
    }

    interface ContactFilledInListener {
        fun contact(name: String, phone: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogType = arguments!!.getInt(EXTRA_TYPE)
        when (dialogType) {
            TYPE_PICK -> return pickDialog
            else -> return fillDialog
        }
    }

    private fun inputValid(name: EditText, phone: EditText): Boolean {
        return !TextUtils.isEmpty(name.text) && Patterns.PHONE.matcher(phone.text).matches()
    }

    companion object {

        val TYPE_PICK = 0
        val TYPE_INSERT_MANUAL = 1
        private val EXTRA_TYPE = "type"

        fun newInstance(dialogType: Int): AddMemberDialog {
            val dialog = AddMemberDialog()
            val args = Bundle()
            args.putInt(EXTRA_TYPE, dialogType)
            dialog.arguments = args
            return dialog
        }
    }

}
