package za.org.grassroot.android.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnParameters;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.rxbinding.RxView;
import za.org.grassroot.android.view.SingleTextMultiButtonView;

/**
 * Created by luke on 2017/08/10.
 */
public class SingleTextMultiButtonFragment extends TextInputFragment implements SingleTextMultiButtonView {

    private static final String BUTTON_GROUP = "BUTTON_GROUP";
    private static final String SHOW_EXPLANATION = "SHOW_EXPLANATION";

    private BtnGrouping btnGrouping;
    private boolean showExplanation;

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

        if (showExplanation) {
            explanation.setText(explanTextRes);
        } else {
            explanation.setVisibility(View.GONE);
        }

        if (btnGrouping != null) {
            subButtons = new Button[] { subButton1, subButton2, subButton3 };
            setButtonViewToFirstRow();
        } else {
            subtitleButtons.setVisibility(View.GONE);
        }

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
    public Observable<CharSequence> bigButtonClicked() {
        return null;
    }

    @Override
    public Observable<BtnReturnBundle> subtitleButtonClicked() {
        Timber.d("returning merge ... how many observables? " + subButtonReturns.size());
        return Observable.merge(subButtonReturns); // maybe
    }

    @Override
    public void scrollToPageOfSubtitles(int page) {

    }
}
