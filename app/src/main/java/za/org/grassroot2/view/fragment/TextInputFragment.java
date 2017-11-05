package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import io.reactivex.Observable;
import za.org.grassroot2.R;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.view.SingleTextView;

/**
 * Created by luke on 2017/08/10.
 */

public abstract class TextInputFragment extends GrassrootFragment implements SingleTextView {

    public static final int MAIN_TEXT_NEXT_ACTION = 100;

    protected static final String HEADER_TEXT_RES = "HEADER_TEXT_RES";
    protected static final String EXPLAN_TEXT_RES = "EXPLAN_TEXT_RES";

    protected int headerTextRes;
    protected int explanTextRes;

    @BindView(R.id.header_text) TextView header;
    @BindView(R.id.explanation_text) @Nullable TextView explanation; // some variants don't have one

    @BindView(R.id.text_input_field) EditText inputText;

    protected static void addStandardArgs(Bundle args, int headerTextRes, int explanTextRes) {
        args.putInt(HEADER_TEXT_RES, headerTextRes);
        args.putInt(EXPLAN_TEXT_RES, explanTextRes);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            headerTextRes = args.getInt(HEADER_TEXT_RES);
            explanTextRes = args.getInt(EXPLAN_TEXT_RES);
        }
    }

    @Override
    public Observable<CharSequence> textInputChanged() {
        return RxTextView.textChanges(inputText);
    }

    @Override
    public void setInputDefault(CharSequence defaultValue) {
        inputText.setText(defaultValue);
    }

    @Override
    public void displayErrorMessage(int messageRes) {
        inputText.setError(getString(messageRes));
    }

    @Override
    public void setInputType(int type) {
        if (inputText != null) {
            inputText.setInputType(type);
        }
    }

    @Override
    public void setImeOptions(int imeOptions) {
        if (inputText != null) {
            inputText.setImeOptions(imeOptions);
        }
    }

    @Override
    public String getInputValue() {
        return inputText.getText().toString();
    }

    @Override
    public void focusOnInput() {
        if (inputText != null) {
            inputText.requestFocus();
        }
    }

}
