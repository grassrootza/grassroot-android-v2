package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.presenter.fragment.MeetingCalledPresenter;

public class MeetingCalledFragment extends GrassrootFragment implements MeetingCalledPresenter.MeetingCalledView {

    @BindView(R.id.text) TextView description;
    @Inject MeetingCalledPresenter presenter;

    private static final String EXTRA_GROUP_UID = "group_uid";

    public static MeetingCalledFragment get(String groupUid) {
        MeetingCalledFragment f = new MeetingCalledFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_GROUP_UID, groupUid);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.attach(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detach(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.loadGroupData(getArguments().getString(EXTRA_GROUP_UID));
    }

    @OnClick(R.id.ok)
    void okClick() {
        getActivity().finish();
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_meeting_called;
    }

    @Override
    public void showDescription(Integer memberCount) {
        description.setText(getResources().getQuantityString(R.plurals.notification_sent, memberCount, memberCount));
    }
}
