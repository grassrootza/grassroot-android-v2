package za.org.grassroot.android.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnParameters;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.view.SingleTextMultiButtonView;

/**
 * Created by luke on 2017/08/10.
 */
public class SingleTextMultiButtonFragment extends TextInputFragment implements SingleTextMultiButtonView {

    private static final String BUTTON_GROUP = "BUTTON_GROUP";
    private BtnGrouping btnGrouping;

    @BindView(R.id.subtitle_buttons_row) ViewGroup subtitleButtons;
    @BindView(R.id.sub_button_1) Button subButton1;
    @BindView(R.id.sub_button_2) Button subButton2;
    @BindView(R.id.sub_button_3) Button subButton3;

    public static SingleTextMultiButtonView newInstance(int headerTextRes,
                                                        int explanTextRes,
                                                        BtnGrouping btnGrouping) {
        SingleTextMultiButtonFragment fragment = new SingleTextMultiButtonFragment();
        Bundle args = new Bundle();
        addStandardArgs(args, headerTextRes, explanTextRes);
        args.putParcelable(BUTTON_GROUP, btnGrouping);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (getArguments() != null) {
            btnGrouping = getArguments().getParcelable(BUTTON_GROUP);
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
        explanation.setText(explanTextRes);
        if (btnGrouping != null) {
            setButtonViewToFirstRow();
        } else {
            subtitleButtons.setVisibility(View.GONE);
        }
        return v;
    }

    // ugly. fix.
    private void setButtonViewToFirstRow() {
        if (btnGrouping != null) {
            BtnParameters[] subButtonInfo = btnGrouping.getFirstSubMenu();
            if (subButtonInfo != null) {
                if (subButtonInfo.length > 0) {
                    subButton1.setText(subButtonInfo[0].getLabelRes());
                }
                if (subButtonInfo.length > 1) {
                    subButton2.setText(subButtonInfo[1].getLabelRes());
                }
                if (subButtonInfo.length > 2) {
                    subButton3.setText(subButtonInfo[2].getLabelRes());
                }
            }
        }
    }

    @Override
    public Observable<CharSequence> bigButtonClicked() {
        return null;
    }

    @Override
    public Observable<BtnReturnBundle> subtitleButtonClicked() {
        return null;
    }

    @Override
    public void scrollToPageOfSubtitles(int page) {

    }
}
