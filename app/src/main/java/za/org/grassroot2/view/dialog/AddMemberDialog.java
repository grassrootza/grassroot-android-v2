package za.org.grassroot2.view.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;

import za.org.grassroot2.R;

public class AddMemberDialog extends DialogFragment {

    public static final  int    TYPE_PICK          = 0;
    public static final  int    TYPE_INSERT_MANUAL = 1;
    private static final String EXTRA_TYPE         = "type";

    private int                     dialogType;
    private AddMemberDialogListener listener;
    private ContactFilledInListener contactListener;

    public void setAddMemberDialogListener(AddMemberDialogListener listener) {
        this.listener = listener;
    }

    public void setContactListener(ContactFilledInListener contactListener) {
        this.contactListener = contactListener;
    }

    public interface AddMemberDialogListener {
        void contactBook();
        void manual();
    }

    public interface ContactFilledInListener {
        void contact(String name, String phone);
    }

    public static AddMemberDialog newInstance(int dialogType) {
        AddMemberDialog dialog = new AddMemberDialog();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TYPE, dialogType);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogType = getArguments().getInt(EXTRA_TYPE);
        switch (dialogType) {
            case TYPE_PICK:
                return getPickDialog();
            default:
                return getFillDialog();
        }
    }

    private Dialog getFillDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_contact_manual_add, null, false);
        EditText name = (EditText) v.findViewById(R.id.nameEdittext);
        EditText phone = (EditText) v.findViewById(R.id.numberEdittext);
        View addButton = v.findViewById(R.id.add);
        RxTextView.textChanges(name).subscribe(charSequence -> addButton.setEnabled(inputValid(name, phone)), Throwable::printStackTrace);
        RxTextView.textChanges(phone).subscribe(charSequence -> addButton.setEnabled(inputValid(name, phone)), Throwable::printStackTrace);
        v.findViewById(R.id.close).setOnClickListener(v1 -> dismiss());
        addButton.setOnClickListener(v1 -> {
            contactListener.contact(name.getText().toString(), phone.getText().toString());
            dismiss();
        });
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }

    private boolean inputValid(EditText name, EditText phone) {
        return !TextUtils.isEmpty(name.getText()) && Patterns.PHONE.matcher(phone.getText()).matches();
    }

    @NonNull
    private Dialog getPickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_contact_selection_method, null, false);
        v.findViewById(R.id.contactBook).setOnClickListener(v1 -> {
            dismiss();
            listener.contactBook();
        });
        v.findViewById(R.id.manual).setOnClickListener(v1 -> {
            dismiss();
            listener.manual();
        });
        v.findViewById(R.id.close).setOnClickListener(v1 -> dismiss());
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }

}
