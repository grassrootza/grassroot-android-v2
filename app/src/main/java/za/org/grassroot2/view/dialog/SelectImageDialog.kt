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
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_select_image.*

import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.view.GrassrootView

class SelectImageDialog : DialogFragment() {

    private lateinit var selectImageDialogEvents: SelectImageDialogEvents

    private val selectImageDialog: Dialog
        get() {
            val builder = AlertDialog.Builder(activity!!)
            val v = LayoutInflater.from(activity).inflate(R.layout.dialog_select_image, null, false)
            var useFragment = false

            arguments?.let {
                (v.findViewById(R.id.msgBody) as TextView).setText(it.getInt(TITLE_RES_ID))
                useFragment = it.getBoolean(USE_TARGET_FRAGMENT)
            }

            try {
                val btnOpenCamera: Button = v.findViewById(R.id.openCamera)
                val btnPickFromGallery: Button = v.findViewById(R.id.pickFromGallery)

                if(useFragment){
                    selectImageDialogEvents = targetFragment as SelectImageDialogEvents
                    btnOpenCamera.setOnClickListener { v1 -> selectImageDialogEvents.openCamera() }
                    btnPickFromGallery.setOnClickListener { v12 -> selectImageDialogEvents.pickImageFromGallery() }
                }else{
                    selectImageDialogEvents = activity as SelectImageDialogEvents
                    btnOpenCamera.setOnClickListener { v1 -> selectImageDialogEvents.openCamera() }
                    btnPickFromGallery.setOnClickListener { v12 -> selectImageDialogEvents.pickImageFromGallery() }
                }
            } catch (e: ClassCastException) {
                Timber.d(e)
            }

            builder.setView(v)
            val d = builder.create()
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return d
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return selectImageDialog
    }

    interface SelectImageDialogEvents  {
        fun openCamera()
        fun pickImageFromGallery()
    }

    companion object {

        private val TITLE_RES_ID = "TITLE_RES_ID"
        private val USE_TARGET_FRAGMENT = "USE_TARGET_FRAGMENT"

        @JvmStatic fun newInstance(dialogText:Int,useTargetFragment:Boolean): SelectImageDialog {
            val dialog = SelectImageDialog()
            val args = Bundle()
            args.putInt(TITLE_RES_ID, dialogText)
            args.putBoolean(USE_TARGET_FRAGMENT,useTargetFragment)
            dialog.arguments = args
            return dialog
        }
    }
}
