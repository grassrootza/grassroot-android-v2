package za.org.grassroot.android.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot.android.R;
import za.org.grassroot.android.rxbinding.RxTextView;
import za.org.grassroot.android.rxbinding.RxTextViewUtils;
import za.org.grassroot.android.rxbinding.RxView;
import za.org.grassroot.android.view.SingleInputView;
import za.org.grassroot.android.view.ViewUtil;

public class SingleTextInputFragment extends Fragment implements SingleInputView {

    private Unbinder unbinder;

    private static final String HEADER_TEXT_RES = "HEADER_TEXT_RES";
    private static final String INPUT_HINT_RES = "INPUT_HINT_RES";
    private static final String BACK_BUTTON_RES = "BACK_BUTTON_RES";
    private static final String NEXT_BUTTON_RES = "NEXT_BUTTON_RES";

    private static final int ACTION_FRAGMENT_ATTACHED = 1;
    private static final int ACTION_FRAGMENT_CREATED = 2;
    private static final int ACTION_FRAGMENT_VIEW_CREATED = 3;

    private int headerTextRes;
    private int inputHintRes;
    private int backBtnRes;
    private int nextBtnRes;

    @BindView(R.id.header_text) TextView header;
    @BindView(R.id.text_input_field) EditText inputText;
    @BindView(R.id.button_back) Button backButton;
    @BindView(R.id.button_next) Button nextButton;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    private PublishSubject<Integer> lifecyclePublisher = PublishSubject.create();

    public SingleTextInputFragment() {
        // Required empty public constructor
    }

    public static SingleTextInputFragment newInstance(int headerTextRes, int inputHintRes,
                                                      int backButtonRes, int nextButtonRes) {
        SingleTextInputFragment fragment = new SingleTextInputFragment();
        Bundle args = new Bundle();
        args.putInt(HEADER_TEXT_RES, headerTextRes);
        args.putInt(INPUT_HINT_RES, inputHintRes);
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
            headerTextRes = args.getInt(HEADER_TEXT_RES);
            inputHintRes = args.getInt(INPUT_HINT_RES);
            backBtnRes = args.getInt(BACK_BUTTON_RES);
            nextBtnRes = args.getInt(NEXT_BUTTON_RES);
        }
        lifecyclePublisher.onNext(ACTION_FRAGMENT_CREATED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_single_text_input, container, false);
        unbinder = ButterKnife.bind(this, v);
        header.setText(headerTextRes);
        inputText.setHint(inputHintRes);
        backButton.setText(backBtnRes);
        nextButton.setText(nextBtnRes);
        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
    public Observable<CharSequence> textInputNextDone() {
        Observable<CharSequence> editTextNext = RxTextView
                .editorActions(inputText, RxTextViewUtils.imeNextDonePredicate())
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

    @Override
    public void setInputDefault(CharSequence defaultValue) {
        inputText.setText(defaultValue);
    }

    @Override
    public void displayErrorMessage(int messageRes) {
        inputText.setError(getString(messageRes));
    }

    @Override
    public void showProgressBar() {
        ViewUtil.safeToggleProgressBar(progressBar, true);
    }

    @Override
    public void closeProgressBar() {
        ViewUtil.safeToggleProgressBar(progressBar, false);
    }
}