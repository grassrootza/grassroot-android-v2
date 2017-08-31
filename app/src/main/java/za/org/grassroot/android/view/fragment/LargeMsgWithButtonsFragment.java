package za.org.grassroot.android.view.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import timber.log.Timber;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnParameters;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.rxbinding.RxView;
import za.org.grassroot.android.view.LargeMsgWithButtonsView;

// todo : abstract 'with button row' maybe (although, some subtle differences, so might be overdoing ...)
public class LargeMsgWithButtonsFragment extends GrassrootFragment implements LargeMsgWithButtonsView {
    private static final String HEADER_TEXT = "header_text";
    private static final String MESSAGE_TEXT = "msg_text";
    private static final String SHOW_BUTTONS = "show_buttons";
    private static final String BUTTON_GROUP = "btn_group";
    private static final String SHOW_SKIP = "show_skip";

    private int headerTextRes;
    private String messageText;
    private boolean showButtons;
    private BtnGrouping btnGrouping;
    private boolean showSkip;

    @BindView(R.id.header_text) TextView headerView;
    @BindView(R.id.message_text) TextView messageView;
    @BindView(R.id.buttons_row) ViewGroup buttonsRow;

    @BindView(R.id.button_1) Button subButton1;
    @BindView(R.id.button_2) Button subButton2;
    @BindView(R.id.button_3) Button subButton3;

    @BindView(R.id.button_skip) Button skipButton;

    // todo: probably want to use a composite disposable instead of this observable list (ugly)
    private Button[] buttons;
    private ArrayList<Observable<BtnReturnBundle>> btnObservables;

    public LargeMsgWithButtonsFragment() {
        // Required empty public constructor
    }

    public static LargeMsgWithButtonsFragment newInstance(int headerRes,
                                                          @NonNull String messageText,
                                                          boolean showButtons,
                                                          @Nullable BtnGrouping btnGrouping,
                                                          boolean showSkip) {
        LargeMsgWithButtonsFragment fragment = new LargeMsgWithButtonsFragment();
        Bundle args = new Bundle();
        args.putInt(HEADER_TEXT, headerRes);
        args.putString(MESSAGE_TEXT, messageText);
        args.putBoolean(SHOW_BUTTONS, showButtons);
        args.putBoolean(SHOW_SKIP, showSkip);
        if (showButtons) {
            if (btnGrouping == null) {
                throw new IllegalArgumentException("If show buttons is set then button grouping must be passed");
            }
            args.putParcelable(BUTTON_GROUP, btnGrouping);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            headerTextRes = getArguments().getInt(HEADER_TEXT);
            messageText = getArguments().getString(MESSAGE_TEXT);
            showButtons = getArguments().getBoolean(SHOW_BUTTONS);
            showSkip = getArguments().getBoolean(SHOW_SKIP);
            btnGrouping = showButtons ? (BtnGrouping) getArguments().getParcelable(BUTTON_GROUP) : null;
        }
        lifecyclePublisher.onNext(ACTION_FRAGMENT_CREATED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_large_msg_with_buttons, container, false);
        unbinder = ButterKnife.bind(this, v);

        headerView.setText(headerTextRes);
        messageView.setText(messageText);
        buttonsRow.setVisibility(showButtons ? View.VISIBLE : View.GONE);

        skipButton.setVisibility(showSkip ? View.VISIBLE : View.GONE);

        if (btnGrouping != null) {
            buttons = new Button[] { subButton1, subButton2, subButton3 };
            setUpButtons();
        }

        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return v;
    }

    private void setUpButtons() {
        Timber.d("setting up buttons");
        btnObservables = new ArrayList<>();
        BtnParameters[] subButtonInfo = btnGrouping.getFirstSubMenu();
        if (subButtonInfo != null) {
            int buttons = subButtonInfo.length;
            for (int i = 0; i < buttons; i++) {
                setupSubButtonView(i, subButtonInfo[i]);
            }
        }
    }

    private void setupSubButtonView(int index, final BtnParameters btnInfo) {
        buttons[index].setText(btnInfo.getLabelRes());
        final int btnActionCode = btnInfo.getActionCode();
        Observable<BtnReturnBundle> observable = RxView.clicks(buttons[index])
                .map(new Function<Object, BtnReturnBundle>() {
                    @Override
                    public BtnReturnBundle apply(@io.reactivex.annotations.NonNull Object o) throws Exception {
                        return new BtnReturnBundle("", btnActionCode);
                    }
                });
        Timber.d("added observable to sub-buttons");
        btnObservables.add(observable);
    }

    // todo : handle this causing errors if no buttons
    public Observable<BtnReturnBundle> buttonClicked() {
        return Observable.merge(btnObservables);
    }

    public Observable<Object> skipClicked() {
        return RxView.clicks(skipButton);
    }

}
