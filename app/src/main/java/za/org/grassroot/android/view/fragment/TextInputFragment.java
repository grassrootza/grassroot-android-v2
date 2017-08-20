package za.org.grassroot.android.view.fragment;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import za.org.grassroot.android.R;
import za.org.grassroot.android.rxbinding.RxTextView;
import za.org.grassroot.android.view.SingleTextView;

/**
 * Created by luke on 2017/08/10.
 */

public class TextInputFragment extends GrassrootFragment implements SingleTextView {

    protected static final String HEADER_TEXT_RES = "HEADER_TEXT_RES";
    protected static final String EXPLAN_TEXT_RES = "EXPLAN_TEXT_RES";

    protected int headerTextRes;
    protected int explanTextRes;

    @BindView(R.id.header_text) TextView header;
    @BindView(R.id.explanation_text) TextView explanation;

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
    public Observable<CharSequence> viewCreated() {
        return lifecyclePublisher
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer == ACTION_FRAGMENT_VIEW_CREATED;
                    }
                })
                .map(new Function<Integer, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Integer integer) throws Exception {
                        return inputText.getText();
                    }
                });
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
    public void focusOnInput() {
        if (inputText != null) {
            inputText.requestFocus();
        }
    }

}
