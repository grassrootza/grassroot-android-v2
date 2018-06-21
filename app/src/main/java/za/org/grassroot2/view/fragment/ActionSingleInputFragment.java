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

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;

public class ActionSingleInputFragment extends GrassrootFragment {

    private static final String EXTRA_TITLE_RES = "title_res";
    private static final String EXTRA_DESC_RES  = "desc_res";
    private static final String EXTRA_HINT_RES  = "hint_res";
    private static final String EXTRA_CAN_SKIP  = "can_skip";
    @BindView(R.id.input)            EditText        input;
    @BindView(R.id.inputContainer)   TextInputLayout inputContainer;
    @BindView(R.id.header)           TextView        title;
    @BindView(R.id.item_description) TextView        description;
    @BindView(R.id.next)             View            next;
    @BindView(R.id.cancel)           Button          cancel;

    private PublishSubject<String> actionSubject = PublishSubject.create();
    private BackNavigationListener listener;
    private boolean                multiLine;

    public static ActionSingleInputFragment newInstance(int resTitle, int resDesc, int resHint, boolean canSkip) {
        ActionSingleInputFragment f = new ActionSingleInputFragment();
        Bundle b = new Bundle();
        b.putInt(EXTRA_TITLE_RES, resTitle);
        b.putInt(EXTRA_DESC_RES, resDesc);
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

    @OnClick(R.id.backNav)
    void back() {
        listener.backPressed();
    }

    @OnClick(R.id.cancel)
    void close() {
        getActivity().finish();
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
        description.setText(getArguments().getInt(EXTRA_DESC_RES));
        if (getArguments().getBoolean(EXTRA_CAN_SKIP, false)) {
            cancel.setText(R.string.button_skip);
            cancel.setOnClickListener(v -> actionSubject.onNext(""));
        }
        disposables.add(RxTextView.textChanges(input).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(charSequence -> next.setEnabled(charSequence.length() > 3), Throwable::printStackTrace));
        RxView.clicks(next).map(o -> input.getText().toString()).subscribe(actionSubject);
        RxTextView.editorActionEvents(input, textViewEditorActionEvent -> textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_DONE && input.length() > 3)
                .map(textViewEditorActionEvent -> input.getText().toString()).subscribe(actionSubject);
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

    public Observable<String> inputAdded() {
        return actionSubject;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_meeting_single_input;
    }

    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    public boolean isMultiLine() {
        return multiLine;
    }
}
