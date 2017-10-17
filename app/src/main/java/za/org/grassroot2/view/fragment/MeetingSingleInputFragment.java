package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.rxbinding.TextViewEditorActionEvent;

/**
 * Created by qbasso on 18.10.2017.
 */

public class MeetingSingleInputFragment extends GrassrootFragment {

    private static final String EXTRA_TITLE_RES = "title_res";
    private static final String EXTRA_DESC_RES  = "desc_res";
    private static final String EXTRA_HINT_RES  = "hint_res";
    @BindView(R.id.input)            EditText        input;
    @BindView(R.id.inputContainer)   TextInputLayout inputContainer;
    @BindView(R.id.header)           TextView        title;
    @BindView(R.id.item_description) TextView        description;
    @BindView(R.id.next)             View            next;

    private PublishSubject<String> actionSubject = PublishSubject.create();
    private BackNavigationListener listener;

    public static MeetingSingleInputFragment newInstance(int resTitle, int resDesc, int resHint) {
        MeetingSingleInputFragment f = new MeetingSingleInputFragment();
        Bundle b = new Bundle();
        b.putInt(EXTRA_TITLE_RES, resTitle);
        b.putInt(EXTRA_DESC_RES, resDesc);
        b.putInt(EXTRA_HINT_RES, resHint);
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
        title.setText(getArguments().getInt(EXTRA_TITLE_RES));
        description.setText(getArguments().getInt(EXTRA_DESC_RES));
        disposables.add(RxTextView.textChanges(input).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(charSequence -> next.setEnabled(charSequence.length() > 3), Throwable::printStackTrace));
        RxView.clicks(next).map(o -> input.getText().toString()).subscribe(actionSubject);
        RxTextView.editorActionEvents(input, textViewEditorActionEvent -> textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_DONE && input.length() > 3)
                .map(textViewEditorActionEvent -> input.getText().toString()).subscribe(actionSubject);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public Observable<String> locationAdded() {
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

}
