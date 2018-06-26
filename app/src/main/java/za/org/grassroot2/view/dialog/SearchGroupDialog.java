package za.org.grassroot2.view.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;

public class SearchGroupDialog extends DialogFragment {

    private PublishSubject<Integer> actionSubject = PublishSubject.create();

    public SearchGroupDialog() {}

    public Observable<Integer> show(FragmentManager manager) {
        show(manager, null);
        return actionSubject;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_multi_option_pick, null, false);
        View.OnClickListener clickListener = v1 -> {
            dismiss();
            actionSubject.onNext(v1.getId());
            actionSubject.onComplete();
        };
        v.findViewById(R.id.dictate).setOnClickListener(clickListener);
        v.findViewById(R.id.create_todo).setOnClickListener(clickListener);
        v.findViewById(R.id.take_vote).setOnClickListener(clickListener);
        v.findViewById(R.id.call_meeting).setOnClickListener(clickListener);
        v.findViewById(R.id.close).setOnClickListener(v1 -> {
            actionSubject.onComplete();
            dismiss();
        });
        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }

}
