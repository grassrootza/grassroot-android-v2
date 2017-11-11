package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.presenter.fragment.ItemCalledPresenter;

public class ItemCreatedFragment extends GrassrootFragment implements ItemCalledPresenter.MeetingCalledView {

    @BindView(R.id.text)  TextView            description;
    @BindView(R.id.title) TextView            title;
    @Inject               ItemCalledPresenter presenter;

    private static final String EXTRA_GROUP_UID = "group_uid";
    private static final String EXTRA_ENTITY_TYPE = "entity_type";

    public static ItemCreatedFragment get(String groupUid, GrassrootEntityType type) {
        ItemCreatedFragment f = new ItemCreatedFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_GROUP_UID, groupUid);
        b.putSerializable(EXTRA_ENTITY_TYPE, type);
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
        presenter.loadGroupData(getArguments().getString(EXTRA_GROUP_UID), (GrassrootEntityType) getArguments().getSerializable(EXTRA_ENTITY_TYPE));
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
        return R.layout.fragment_item_created;
    }

    @Override
    public void showDescription(Integer memberCount, GrassrootEntityType type) {
        if (type == GrassrootEntityType.MEETING) {
            title.setText(R.string.meeting_called);
        } else if (type == GrassrootEntityType.LIVE_WIRE_ALERT) {
            title.setText(R.string.lwire_alert_sent);
        }
        description.setText(getResources().getQuantityString(R.plurals.notification_sent, memberCount, memberCount));
    }
}
