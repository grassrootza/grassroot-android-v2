package za.org.grassroot2.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.fragment.FragmentComponent;
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
    protected void onInject(ActivityComponent activityComponent) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        header.setText(headerTextRes); // res value set via super calls
        inputText.setHint(inputHintRes);
        nextButton.setText(nextBtnRes);
        skipButton.setText(skipBtnRes);
        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_long_text_input;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disposables.add(RxView.clicks(skipButton).subscribe(o -> {
            EventBus.getDefault().post(new LongInputEvent(null));
        }));
        disposables.add(RxView.clicks(nextButton).map(o -> inputText.getText()).subscribe(editable -> EventBus.getDefault().post(new LongInputEvent(editable.toString()))));
    }

    @Override
    public void toggleNextDoneButton(boolean enabled) {
        nextButton.setEnabled(enabled);
    }

    @Override
    public void toggleBackOtherButton(boolean enabled) {
        skipButton.setEnabled(enabled);
    }

    public static class LongInputEvent {
        public final String s;

        public LongInputEvent(String s) {
            this.s = s;
        }
    }
}
