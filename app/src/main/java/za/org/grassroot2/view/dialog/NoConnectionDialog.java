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

import za.org.grassroot2.R;

public class NoConnectionDialog extends DialogFragment {

    public static final  int    TYPE_NOT_AUTHORIZED = 0;
    public static final  int    TYPE_AUTHORIZED     = 1;
    private static final String EXTRA_TYPE          = "type";
    private int dialogType;

    public static DialogFragment newInstance(int dialogType) {
        NoConnectionDialog dialog = new NoConnectionDialog();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TYPE, dialogType);
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
        dialogType = getArguments().getInt(EXTRA_TYPE);
        switch (dialogType) {
            case TYPE_NOT_AUTHORIZED:
                return getNotAuthorizedDialog();
            default:
                return getAuthorizedDialog();
        }
    }

    @NonNull
    private Dialog getNotAuthorizedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_no_connection_not_authorized, null, false);
        v.findViewById(R.id.close).setOnClickListener(v1 -> dismiss());
        v.findViewById(R.id.done).setOnClickListener(v1 -> dismiss());
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }

    @NonNull
    private Dialog getAuthorizedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_no_connection_authorized, null, false);
        v.findViewById(R.id.continueButton).setOnClickListener(v1 -> dismiss());
        v.findViewById(R.id.retryButton).setOnClickListener(v1 -> dismiss());
        v.findViewById(R.id.close).setOnClickListener(v1 -> dismiss());
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }
}
