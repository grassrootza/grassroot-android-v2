package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.util.DateFormatter;

public class MeetingDateConfirmFragment extends GrassrootFragment {

    private static final String EXTRA_DATE = "date";
    @BindView(R.id.date) TextView date;
    @BindView(R.id.year) TextView year;
    @BindView(R.id.time) TextView time;
    @BindView(R.id.next) View     next;

    private PublishSubject<Long> actionSubject = PublishSubject.create();
    private BackNavigationListener listener;
    private long                   dateToConfirm;

    public static MeetingDateConfirmFragment newInstance(long date) {
        MeetingDateConfirmFragment f = new MeetingDateConfirmFragment();
        Bundle b = new Bundle();
        b.putLong(EXTRA_DATE, date);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (BackNavigationListener) getActivity();
    }

    @OnClick({R.id.backNav, R.id.cancel})
    void back() {
        listener.backPressedAndRemoveLast();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateToConfirm = getArguments().getLong(EXTRA_DATE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        date.setText(DateFormatter.formatDate(dateToConfirm));
        year.setText(DateFormatter.formatYear(dateToConfirm));
        time.setText(DateFormatter.formatTime(dateToConfirm));
        RxView.clicks(next).map(o -> dateToConfirm).subscribe(actionSubject);
    }

    public Observable<Long> meetingDateConfirmed() {
        return actionSubject;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_meeting_date_confirm;
    }

}
