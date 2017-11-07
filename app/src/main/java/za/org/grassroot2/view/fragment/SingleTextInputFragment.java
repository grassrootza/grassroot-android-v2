package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import io.reactivex.Observable;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.rxbinding.RxViewUtils;
import za.org.grassroot2.view.SingleInputNextOtherView;

public class SingleTextInputFragment extends TextInputFragment implements SingleInputNextOtherView {

    private static final String INPUT_LABEL_RES = "INPUT_LABEL_RES";
    private static final String INPUT_HINT_RES = "INPUT_HINT_RES";
    private static final String NEXT_BUTTON_RES = "NEXT_BUTTON_RES";

    private int nextBtnRes;
    private int inputLabelRes;
    private int inputHintRes;

    @BindView(R.id.explanation_text) TextView explanation;
    @BindView(R.id.button_back)
    ImageButton backButton;
    @BindView(R.id.button_next) Button nextButton;
    @BindView(R.id.inputLabel)
    TextView inputLabel;
    @BindView(R.id.text_input_field)
    EditText inputField;

    public SingleTextInputFragment() {
    }

    public static SingleTextInputFragment newInstance(int headerTextRes, int explanTextRes, int inputLabelRes, int inputHintRes, int nextButtonRes) {
        SingleTextInputFragment fragment = new SingleTextInputFragment();
        Bundle args = new Bundle();
        addStandardArgs(args, headerTextRes, explanTextRes);
        args.putInt(INPUT_LABEL_RES, inputLabelRes);
        args.putInt(INPUT_HINT_RES, inputHintRes);
        args.putInt(NEXT_BUTTON_RES, nextButtonRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            nextBtnRes = args.getInt(NEXT_BUTTON_RES);
            inputLabelRes = args.getInt(INPUT_LABEL_RES);
            inputHintRes = args.getInt(INPUT_HINT_RES);
        }
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        header.setText(headerTextRes);
        explanation.setText(explanTextRes);

        nextButton.setText(nextBtnRes);
        inputLabel.setText(inputLabelRes);
        inputField.setHint(inputHintRes);

        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Observable<CharSequence> editTextNext = RxTextView
                .editorActions(inputText, RxViewUtils.imeNextDonePredicate())
                .filter(integer -> {
                    return inputText != null; // because Android
                })
                .map(integer -> inputText.getText());
        Observable<CharSequence> nextButtonClicked = RxView
                .clicks(nextButton).map(o -> inputText.getText());

        Observable<CharSequence> backClick = RxView.clicks(backButton)
                .map(o -> inputText.getText());

        disposables.add(RxTextView.textChanges(inputText).subscribe(charSequence -> {
            if (charSequence.length()>1) {
                toggleNextDoneButton(true);
            }
        }, Throwable::printStackTrace));

        disposables.add(Observable.merge(editTextNext, nextButtonClicked).subscribe(charSequence ->
                EventBus.getDefault().post(new SingleInputTextEvent(this, SingleInputTextEventType.DONE, charSequence)), Throwable::printStackTrace));

        disposables.add(backClick.subscribe(charSequence ->
                EventBus.getDefault().post(new SingleInputTextEvent(this, SingleInputTextEventType.BACK, "")), Throwable::printStackTrace));
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_single_text_input;
    }

    @Override
    public void toggleNextDoneButton(boolean enabled) {
        nextButton.setEnabled(enabled);
    }

    @Override
    public void toggleBackOtherButton(boolean enabled) {
        backButton.setEnabled(enabled);
        backButton.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }


    public enum SingleInputTextEventType {CHANGE, DONE, BACK}

    public static class SingleInputTextEvent {
        public final GrassrootFragment source;
        public final SingleInputTextEventType type;
        public final CharSequence value;

        public SingleInputTextEvent(GrassrootFragment source, SingleInputTextEventType type, CharSequence value) {
            this.source = source;
            this.type = type;
            this.value = value;
        }
    }
}