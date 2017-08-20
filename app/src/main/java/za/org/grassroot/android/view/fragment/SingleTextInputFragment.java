package za.org.grassroot.android.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import za.org.grassroot.android.R;
import za.org.grassroot.android.rxbinding.RxTextView;
import za.org.grassroot.android.rxbinding.RxView;
import za.org.grassroot.android.rxbinding.RxViewUtils;
import za.org.grassroot.android.view.SingleInputNextOtherView;

public class SingleTextInputFragment extends TextInputFragment implements SingleInputNextOtherView {

    private static final String BACK_BUTTON_RES = "BACK_BUTTON_RES";
    private static final String NEXT_BUTTON_RES = "NEXT_BUTTON_RES";

    private int backBtnRes;
    private int nextBtnRes;

    @BindView(R.id.explanation_text) TextView explanation;
    @BindView(R.id.button_back) Button backButton;
    @BindView(R.id.button_next) Button nextButton;

    public SingleTextInputFragment() {
        // Required empty public constructor
    }

    public static SingleTextInputFragment newInstance(int headerTextRes, int explanTextRes,
                                                      int backButtonRes, int nextButtonRes) {
        SingleTextInputFragment fragment = new SingleTextInputFragment();
        Bundle args = new Bundle();
        addStandardArgs(args, headerTextRes, explanTextRes);
        args.putInt(BACK_BUTTON_RES, backButtonRes);
        args.putInt(NEXT_BUTTON_RES, nextButtonRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            backBtnRes = args.getInt(BACK_BUTTON_RES);
            nextBtnRes = args.getInt(NEXT_BUTTON_RES);
        }
        // leave this down here so it executes right at the end
        lifecyclePublisher.onNext(ACTION_FRAGMENT_CREATED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_single_text_input, container, false);
        unbinder = ButterKnife.bind(this, v);
        header.setText(headerTextRes);
        explanation.setText(explanTextRes);
        backButton.setText(backBtnRes);
        nextButton.setText(nextBtnRes);
        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    @Override
    public Observable<CharSequence> textInputNextDone() {
        Observable<CharSequence> editTextNext = RxTextView
                .editorActions(inputText, RxViewUtils.imeNextDonePredicate())
                .map(new Function<Integer, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Integer integer) throws Exception {
                        return inputText.getText();
                    }
                });
        Observable<CharSequence> nextButtonClicked = RxView
                .clicks(nextButton).map(new Function<Object, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Object o) throws Exception {
                        return inputText.getText();
                    }
                });
        return Observable.merge(editTextNext, nextButtonClicked);
    }

    @Override
    public Observable<CharSequence> textInputBackOther() {
        return RxView.clicks(backButton)
                .map(new Function<Object, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Object o) throws Exception {
                        return inputText.getText();
                    }
                });
    }

    @Override
    public void toggleNextDoneButton(boolean enabled) {
        nextButton.setEnabled(enabled);
    }

    @Override
    public void toggleBackOtherButton(boolean enabled) {
        backButton.setEnabled(enabled);
    }

}