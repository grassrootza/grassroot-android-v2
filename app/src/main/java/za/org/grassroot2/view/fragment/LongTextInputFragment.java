package za.org.grassroot2.view.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.model.dto.BtnReturnBundle;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.view.SingleInputNextOtherView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LongTextInputFragment extends TextInputFragment implements SingleInputNextOtherView {

    private static final String INPUT_HINT_RES = "INPUT_HINT_RES";
    private static final String SKIP_BUTTON_RES = "SKIP_BUTTON_RES";
    private static final String NEXT_BUTTON_RES = "NEXT_BUTTON_RES";

    private int inputHintRes;
    private int skipBtnRes;
    private int nextBtnRes;

    @BindView(R.id.button_skip) Button skipButton;
    @BindView(R.id.button_next) Button nextButton;

    public static LongTextInputFragment newInstance(int headerRes, int textHintRes,
                                                    int skipButtonRes, int nextButtonRes) {
        LongTextInputFragment fragment = new LongTextInputFragment();
        Bundle args = new Bundle();
        args.putInt(HEADER_TEXT_RES, headerRes);
        args.putInt(INPUT_HINT_RES, textHintRes);
        args.putInt(SKIP_BUTTON_RES, skipButtonRes);
        args.putInt(NEXT_BUTTON_RES, nextButtonRes);
        fragment.setArguments(args);
        return fragment;
    }

    public LongTextInputFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            skipBtnRes = args.getInt(SKIP_BUTTON_RES);
            nextBtnRes = args.getInt(NEXT_BUTTON_RES);
            inputHintRes = args.getInt(INPUT_HINT_RES);
        }
        lifecyclePublisher.onNext(ACTION_FRAGMENT_CREATED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_long_text_input, container, false);
        unbinder = ButterKnife.bind(this, v);
        header.setText(headerTextRes); // res value set via super calls
        inputText.setHint(inputHintRes);
        nextButton.setText(nextBtnRes);
        skipButton.setText(skipBtnRes);
        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    @Override
    public void toggleNextDoneButton(boolean enabled) {
        nextButton.setEnabled(enabled);
    }

    @Override
    public void toggleBackOtherButton(boolean enabled) {
        skipButton.setEnabled(enabled);
    }

    // todo : clean up inheritance in here
    @Override
    public Observable<CharSequence> textInputNextDone() {
        // on this one we want to let people do done, and then confirm, rather than return immediately on input done
        return RxView.clicks(nextButton)
                .map(new Function<Object, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Object o) throws Exception {
                        return inputText.getText();
                    }
                });
    }

    @Override
    public Observable<BtnReturnBundle> mainTextNext() {
        Timber.e("subscribed in here");
        return textInputNextDone().map(new Function<CharSequence, BtnReturnBundle>() {
            @Override
            public BtnReturnBundle apply(@NonNull CharSequence sequence) throws Exception {
                return new BtnReturnBundle(sequence, MAIN_TEXT_NEXT_ACTION);
            }
        });
    }

    @Override
    public Observable<CharSequence> textInputBackOther() {
        return RxView.clicks(skipButton)
                .map(new Function<Object, CharSequence>() {
                    @Override
                    public CharSequence apply(@NonNull Object o) throws Exception {
                        return inputText.getText();
                    }
                });
    }

    public Observable<Object> skipClicked() {
        return RxView.clicks(skipButton);
    }
}
