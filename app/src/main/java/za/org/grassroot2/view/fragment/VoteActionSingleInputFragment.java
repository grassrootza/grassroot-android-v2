package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.rxbinding.RxView;

public class VoteActionSingleInputFragment extends GrassrootFragment{

    private static final String EXTRA_TITLE_RES = "title_res";
    private static final String EXTRA_HINT_RES  = "hint_res";
    private static final String EXTRA_CAN_SKIP  = "can_skip";
    private static final String EXTRA_OPTIONS_LIST = "options_list";
    @BindView(R.id.input)
    EditText input;
    @BindView(R.id.inputContainer)
    TextInputLayout inputContainer;
    @BindView(R.id.vote_options_header)
    TextView title;
    @BindView(R.id.add_vote_option_next)
    View next;
    @BindView(R.id.add_vote_option_cancel)
    Button cancel;
    @BindView(R.id.buttons)
    Button add;

    List<String> chosenOptionsList = new List<String>;

    private PublishSubject<Array> chosenOptions = PublishSubject.create();
    private BackNavigationListener listener;
    private boolean                multiLine;

    public static VoteActionSingleInputFragment newInstance(List<String> optionsList/*make null safe*/, int resTitle, int resHint, boolean canSkip) {
        VoteActionSingleInputFragment f = new VoteActionSingleInputFragment();
        Bundle b = new Bundle();
        b.putInt(EXTRA_TITLE_RES, resTitle);
        b.putInt(EXTRA_HINT_RES, resHint);
        b.putBoolean(EXTRA_CAN_SKIP, canSkip);
        if (!optionsList.equals(null)) {
            b.putIntegerArrayList(EXTRA_OPTIONS_LIST, optionsList);
        }
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (BackNavigationListener) getActivity();
    }

    @OnClick(R.id.backNav)
    void back() {
        listener.backPressed();
    }

    @OnClick(R.id.cancel)
    void close() {
        getActivity().finish();
    }

    @OnClick(R.id.add_vote_response_button)
    void add() {
        String voteOptionInput = inputContainer.getEditText().getText().toString();
        chosenOptionsList.add(voteOptionInput);
        fragment = VoteActionSingleInputFragment.newInstance(chosenOptionsList, getArguments().getInt(EXTRA_TITLE_RES), getArguments().getInt(EXTRA_HINT_RES), getArguments().getBoolean(EXTRA_CAN_SKIP));
        // How to call observable in each new created instance...
        // When user clicks next, chosenOptions should bubble back to the first instance and publish to subscriber
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputContainer.setHint(getString(getArguments().getInt(EXTRA_HINT_RES)));
        setMultilineIfRequired();
        title.setText(getArguments().getInt(EXTRA_TITLE_RES));
        if (getArguments().getBoolean(EXTRA_CAN_SKIP, false)) {
            cancel.setText(R.string.button_skip);
            cancel.setOnClickListener(v -> input.onNext(""));
        }
        disposables.add(RxTextView.textChanges(input).debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> next.setEnabled(charSequence.length() > 3), Throwable::printStackTrace));
    }

    private void setMultilineIfRequired() {
        if (multiLine) {
            input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setSingleLine(false);
            input.setGravity(Gravity.LEFT | Gravity.TOP);
            input.setMaxLines(5);
            input.setMinLines(5);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public Observable<Array> inputAdded() {
        return chosenOptions;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_add_vote_responses_single_input;
    }

    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    public boolean isMultiLine() {
        return multiLine;
    }
}

