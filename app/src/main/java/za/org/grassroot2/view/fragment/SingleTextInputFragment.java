package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import io.reactivex.Observable;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.rxbinding.RxViewUtils;
import za.org.grassroot2.view.SingleInputNextOtherView;

public class SingleTextInputFragment extends TextInputFragment implements SingleInputNextOtherView {

    private static final String BACK_BUTTON_RES = "BACK_BUTTON_RES";
    private static final String NEXT_BUTTON_RES = "NEXT_BUTTON_RES";

    private int backBtnRes;
    private int nextBtnRes;

    @BindView(R.id.explanation_text) TextView explanation;
    @BindView(R.id.button_back) Button backButton;
    @BindView(R.id.button_next) Button nextButton;

    public SingleTextInputFragment() {
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
        backButton.setText(backBtnRes);
        nextButton.setText(nextBtnRes);
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
        disposables.add(Observable.merge(editTextNext, nextButtonClicked, backClick).subscribe(charSequence ->
                EventBus.getDefault().post(new SingleInputTextEvent(charSequence)), Throwable::printStackTrace));
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
    }

    public static class SingleInputTextEvent {
        public final CharSequence value;

        public SingleInputTextEvent(CharSequence charSequence) {
            this.value = charSequence;
        }
    }
}