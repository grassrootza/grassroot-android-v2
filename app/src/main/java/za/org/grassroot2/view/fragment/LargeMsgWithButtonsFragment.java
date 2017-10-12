package za.org.grassroot2.view.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import io.reactivex.Observable;
import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.model.dto.BtnParameters;
import za.org.grassroot2.model.dto.BtnReturnBundle;
import za.org.grassroot2.rxbinding.RxView;

// todo : abstract 'with button row' maybe (although, some subtle differences, so might be overdoing ...)
public class LargeMsgWithButtonsFragment extends GrassrootFragment {
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
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
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

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_large_msg_with_buttons;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disposables.add(Observable.merge(btnObservables).subscribe(btnReturnBundle -> {
            EventBus.getDefault().post(new BtnClickEvent(btnReturnBundle));
        }, Throwable::printStackTrace));
        disposables.add(RxView.clicks(skipButton).subscribe(o -> {
            EventBus.getDefault().post(new BtnClickEvent(null));
        }, Throwable::printStackTrace));
    }

    private void setupSubButtonView(int index, final BtnParameters btnInfo) {
        buttons[index].setText(btnInfo.getLabelRes());
        final int btnActionCode = btnInfo.getActionCode();
        Observable<BtnReturnBundle> observable = RxView.clicks(buttons[index])
                .map(o -> new BtnReturnBundle("", btnActionCode));
        Timber.d("added observable to sub-buttons");
        btnObservables.add(observable);
    }

    public static class BtnClickEvent {
        public final BtnReturnBundle value;

        public BtnClickEvent(BtnReturnBundle grouping) {
            value = grouping;
        }
    }
}
