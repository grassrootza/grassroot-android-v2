package za.org.grassroot2.view.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import za.org.grassroot2.R;

public class GenericMessageDialog extends DialogFragment {

    private static String MSG_BODY = "MSG_BODY";

    public static DialogFragment newInstance(String text) {
        GenericMessageDialog dialog = new GenericMessageDialog();
        Bundle args = new Bundle();
        args.putString(MSG_BODY, text);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getMsgDialog();
    }

    @NonNull
    private Dialog getMsgDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_generic_message, null, false);
        String text = getArguments().getString(MSG_BODY);
        ((TextView) v.findViewById(R.id.msgBody)).setText(text);
        v.findViewById(R.id.close).setOnClickListener(v1 -> dismiss());
        v.findViewById(R.id.done).setOnClickListener(v1 -> dismiss());
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }


}
