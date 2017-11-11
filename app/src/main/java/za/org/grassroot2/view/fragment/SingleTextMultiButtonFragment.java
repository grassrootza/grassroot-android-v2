package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.fragment.FragmentComponent;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.model.dto.BtnParameters;
import za.org.grassroot2.model.dto.BtnReturnBundle;
import za.org.grassroot2.presenter.fragment.SingleTextMultiButtonPresenter;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.rxbinding.RxViewUtils;

/**
 * Created by luke on 2017/08/10.
 */
public class SingleTextMultiButtonFragment extends TextInputFragment implements SingleTextMultiButtonPresenter.SingleTextMultiButtonView {

    private static final String BUTTON_GROUP = "BUTTON_GROUP";
    private static final String SHOW_EXPLANATION = "SHOW_EXPLANATION";

    private BtnGrouping btnGrouping;
    private boolean showExplanation;

    @BindView(R.id.text_input_field) TextView mainTextView;
    @BindView(R.id.subtitle_buttons_row) ViewGroup subtitleButtons;

    @BindView(R.id.sub_button_1) Button subButton1;
    @BindView(R.id.sub_button_2) Button subButton2;
    @BindView(R.id.sub_button_3) Button subButton3;

    @Inject SingleTextMultiButtonPresenter presenter;

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
    }

    @Override
    protected void onInject(ActivityComponent application) {
        application.inject(this);
        presenter.attach(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        header.setText(headerTextRes);
        if (explanation != null) {
            if (showExplanation) {
                explanation.setText(explanTextRes);
            } else {
                explanation.setVisibility(View.GONE);
            }
        }

        if (btnGrouping != null) {
            setupButtons();
        } else {
            subtitleButtons.setVisibility(View.GONE);
        }

        Timber.e("calling action fragment view created");
        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.onViewCreated();
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_text_input_multi_button;
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
                    .map(o -> new BtnReturnBundle(inputText.getText(), btnActionCode));
            Timber.d("added observable to sub-buttons");
            subButtonReturns.add(observable);
        }
    }

    @Override
    public Observable<CharSequence> inputTextDone() {
        return RxViewUtils.nullSafeTextViewNextDone(mainTextView);
    }

    @Override
    public void setupButtons() {
        subButtons = new Button[] { subButton1, subButton2, subButton3 };
        setButtonViewToFirstRow();
    }

}
