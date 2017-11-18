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

public class GenericErrorDialog extends DialogFragment {

    private static String MSG_RES_ID_ARG = "MSG_RES_ID_ARG";

    public static DialogFragment newInstance(int dialogType) {
        GenericErrorDialog dialog = new GenericErrorDialog();
        Bundle args = new Bundle();
        args.putInt(MSG_RES_ID_ARG, dialogType);
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
        return getErrorDialog();
    }

    @NonNull
    private Dialog getErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_generic_error, null, false);
        int errorMsgResID = getArguments().getInt(MSG_RES_ID_ARG);
        ((TextView) v.findViewById(R.id.title)).setText(errorMsgResID);
        v.findViewById(R.id.close).setOnClickListener(v1 -> dismiss());
        v.findViewById(R.id.done).setOnClickListener(v1 -> dismiss());
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }


}
