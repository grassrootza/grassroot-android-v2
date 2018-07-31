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
import android.widget.Button;

import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.view.GrassrootView;

public class SelectImageDialog extends DialogFragment {

    public Button btnOpenCamera, btnPickFromGallery;

    SelectImageDialogEvents selectImageDialogEvents;

    public static SelectImageDialog newInstance(){
        return new SelectImageDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getSelectImageDialog();
    }

    @NonNull
    private Dialog getSelectImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_select_image, null, false);

        try{
            selectImageDialogEvents = (SelectImageDialogEvents)getActivity();
        }catch (ClassCastException e){
            Timber.d(e);
        }

        btnOpenCamera = v.findViewById(R.id.openCamera);
        btnOpenCamera.setOnClickListener(v1 -> selectImageDialogEvents.openCamera());

        btnPickFromGallery = v.findViewById(R.id.pickFromGallery);
        btnPickFromGallery.setOnClickListener(v12 -> selectImageDialogEvents.pickImageFromGallery());


        builder.setView(v);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }

    public interface SelectImageDialogEvents extends GrassrootView{
        void openCamera();
        void pickImageFromGallery();
    }
}
