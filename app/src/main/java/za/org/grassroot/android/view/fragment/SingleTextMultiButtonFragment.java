package za.org.grassroot.android.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnParameters;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.rxbinding.RxView;
import za.org.grassroot.android.rxbinding.RxViewUtils;
import za.org.grassroot.android.view.SingleTextMultiButtonView;

/**
 * Created by luke on 2017/08/10.
 */
public class SingleTextMultiButtonFragment extends TextInputFragment implements SingleTextMultiButtonView {

    private static final String BUTTON_GROUP = "BUTTON_GROUP";
    private static final String SHOW_EXPLANATION = "SHOW_EXPLANATION";

    private BtnGrouping btnGrouping;
    private boolean showExplanation;

    @BindView(R.id.text_input_field) TextView mainTextView;
    @BindView(R.id.subtitle_buttons_row) ViewGroup subtitleButtons;

    @BindView(R.id.sub_button_1) Button subButton1;
    @BindView(R.id.sub_button_2) Button subButton2;
    @BindView(R.id.sub_button_3) Button subButton3;

    private Button[] subButtons;
    private ArrayList<Observable<BtnReturnBundle>> subButtonReturns;

    public static SingleTextMultiButtonFragment newInstance(int headerTextRes,
                                                        boolean showExplanText,
                                                        int explanTextRes,
                                                        BtnGrouping btnGrouping) {
        SingleTextMultiButtonFragment fragment = new SingleTextMultiButtonFragment();
        Bundle args = new Bundle();
        addStandardArgs(args, headerTextRes, explanTextRes);
        args.putBoolean(SHOW_EXPLANATION, showExplanText);
        args.putParcelable(BUTTON_GROUP, btnGrouping);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        subButtonReturns = new ArrayList<>(); // usual NPE paranoia (if not, would only initialize later)
        if (getArguments() != null) {
            btnGrouping = getArguments().getParcelable(BUTTON_GROUP);
            showExplanation = getArguments().getBoolean(SHOW_EXPLANATION, false);
        }
        // leave this down here so it executes right at the end
        lifecyclePublisher.onNext(ACTION_FRAGMENT_CREATED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text_input_multi_button, container, false);
        unbinder = ButterKnife.bind(this, v);
        header.setText(headerTextRes);

        if (explanation != null) {
            if (showExplanation) {
                explanation.setText(explanTextRes);
            } else {
                explanation.setVisibility(View.GONE);
            }
        }

        if (btnGrouping != null) {
            subButtons = new Button[] { subButton1, subButton2, subButton3 };
            setButtonViewToFirstRow();
        } else {
            subtitleButtons.setVisibility(View.GONE);
        }

        Timber.e("calling action fragment view created");
        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    private void setButtonViewToFirstRow() {
        if (btnGrouping != null) {
            BtnParameters[] subButtonInfo = btnGrouping.getFirstSubMenu();
            if (subButtonInfo != null) {
                int buttons = subButtonInfo.length;
                for (int i = 0; i < buttons; i++) {
                    setupSubButtonView(i, subButtonInfo[i]);
                }
            }
        }
    }

    private void setupSubButtonView(int index, final BtnParameters btnInfo) {
        if (subButtons != null && subButtons[index] != null) {
            subButtons[index].setText(btnInfo.getLabelRes());
            final int btnActionCode = btnInfo.getActionCode();
            Observable<BtnReturnBundle> observable = RxView.clicks(subButtons[index])
                    .map(new Function<Object, BtnReturnBundle>() {
                        @Override
                        public BtnReturnBundle apply(@NonNull Object o) throws Exception {
                            return new BtnReturnBundle(inputText.getText(), btnActionCode);
                        }
                    });
            Timber.d("added observable to sub-buttons");
            subButtonReturns.add(observable);
        }
    }

    @Override
    public Observable<BtnReturnBundle> mainTextNext() {
        Timber.e("returning main text next");
        return RxViewUtils.nullSafeTextViewNextDone(mainTextView)
                .concatMap(new Function<CharSequence, ObservableSource<? extends BtnReturnBundle>>() {
                    @Override
                    public ObservableSource<? extends BtnReturnBundle> apply(@NonNull CharSequence sequence) throws Exception {
                        Timber.e("main text next clicked");
                        return Observable.just(new BtnReturnBundle(sequence, MAIN_TEXT_NEXT_ACTION));
                    }
                });
    }

    @Override
    public Observable<CharSequence> bigButtonClicked() {
        return null;
    }

    // todo: check behaviour on back pressed -- looks like observables are being duplicated
    @Override
    public Observable<BtnReturnBundle> subtitleButtonClicked() {
        if (subButtonReturns != null) {
            Timber.d("returning merge ... how many observables? " + subButtonReturns.size());
            return Observable.merge(subButtonReturns); // maybe
        } else {
            Timber.e("well, should not have this ...");
            return Observable.empty();
        }
    }

    @Override
    public void scrollToPageOfSubtitles(int page) {

    }
}
