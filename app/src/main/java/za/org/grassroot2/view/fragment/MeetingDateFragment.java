package za.org.grassroot2.view.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.presenter.fragment.MeetingDatePresenter;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.rxbinding.RxView;

public class MeetingDateFragment extends GrassrootFragment implements MeetingDatePresenter.MeetingDateView {

    @BindView(R.id.dateInput) EditText date;
    @BindView(R.id.next)      View     next;

    @Inject MeetingDatePresenter presenter;
    private Calendar seletedDate = Calendar.getInstance();

    private PublishSubject<Long> actionSubject = PublishSubject.create();
    private BackNavigationListener listener;

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

    @OnClick(R.id.pickDate)
    void pickDate() {
        Calendar c = Calendar.getInstance();
        Dialog d = new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
            seletedDate.set(year, month, dayOfMonth);
            pickTime();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        d.show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog tpd = new TimePickerDialog(getActivity(), (view, hourOfDay, minute) -> {
            seletedDate.set(Calendar.HOUR, hourOfDay);
            seletedDate.set(Calendar.MINUTE, minute);
            actionSubject.onNext(seletedDate.getTimeInMillis());
        }, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false);
        tpd.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.attach(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.onViewCreated();
        disposables.add(RxTextView.textChanges(date).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(charSequence -> next.setEnabled(charSequence.length() > 0), Throwable::printStackTrace));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
    }

    public Observable<Long> meetingDatePicked() {
        return actionSubject;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_meeting_date;
    }

    @Override
    public Observable<String> dateInputConfirmed() {
        return Observable.merge(RxView.clicks(next).map(o -> date.getText().toString()), RxTextView.editorActionEvents(date).filter(textViewEditorActionEvent -> textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_DONE)
                .map(textViewEditorActionEvent -> date.getText().toString())).subscribeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void renderDate(Long timestamp) {
        closeProgressBar();
        seletedDate.setTimeInMillis(timestamp);
        actionSubject.onNext(seletedDate.getTimeInMillis());
    }

    @Override
    public void showDatePicker() {
        pickDate();
    }
}
