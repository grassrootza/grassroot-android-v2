package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.rxbinding.RxView;

public class VoteActionSingleInputFragment extends GrassrootFragment{

    private static final String EXTRA_TITLE_RES = "title_res";
    private static final String EXTRA_HINT_RES  = "hint_res";
    private static final String EXTRA_CAN_SKIP  = "can_skip";
    @BindView(R.id.vote_input)
    EditText input;
    @BindView(R.id.add_vote_options_input_container)
    TextInputLayout inputContainer;
    @BindView(R.id.vote_options_header)
    TextView title;
    @BindView(R.id.add_vote_option_next)
    View next;
    @BindView(R.id.add_vote_option_cancel)
    Button cancel;
    @BindView(R.id.vote_options_ist_view)
    ListView listView;
    @BindView(R.id.add_vote_response_button)
    Button add;


    List<String> chosenOptionsList = new ArrayList<String>();

    private PublishSubject<List<String>> chosenOptions = PublishSubject.create();
    private BackNavigationListener listener;
    private boolean                multiLine;

    public static VoteActionSingleInputFragment newInstance(int resTitle, int resHint, boolean canSkip) {
        VoteActionSingleInputFragment f = new VoteActionSingleInputFragment();
        Bundle b = new Bundle();
        b.putInt(EXTRA_TITLE_RES, resTitle);
        b.putInt(EXTRA_HINT_RES, resHint);
        b.putBoolean(EXTRA_CAN_SKIP, canSkip);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (BackNavigationListener) getActivity();
    }

    @OnClick(R.id.vote_back_nav)
    void back() {
        listener.backPressed();
    }

    @OnClick(R.id.add_vote_option_cancel)
    void close() {
        getActivity().finish();
    }

    @OnClick(R.id.add_vote_response_button)
    void add() {
        String voteOptionInput = inputContainer.getEditText().getText().toString();
        chosenOptionsList.add(voteOptionInput);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),  R.layout.simple_single_item, chosenOptionsList);
        listView.setAdapter(arrayAdapter);
        inputContainer.getEditText().setText("");
        arrayAdapter.notifyDataSetChanged();
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
        disposables.add(RxTextView.textChanges(input).debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> next.setEnabled(true), Throwable::printStackTrace));

        RxView.clicks(next).observeOn(AndroidSchedulers.mainThread())
                .map(clickEvent -> chosenOptionsList).subscribe(chosenOptions);
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

    public Observable<List<String>> inputAdded() {
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

