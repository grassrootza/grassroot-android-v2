package za.org.grassroot2.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.GroupPermission;
import za.org.grassroot2.presenter.CreateActionPresenter;
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter;
import za.org.grassroot2.view.dialog.CreateActionFragment;
import za.org.grassroot2.view.fragment.BackNavigationListener;
import za.org.grassroot2.view.fragment.GroupSelectionFragment;
import za.org.grassroot2.view.fragment.MeetingCalledFragment;
import za.org.grassroot2.view.fragment.MeetingDateConfirmFragment;
import za.org.grassroot2.view.fragment.MeetingDateFragment;
import za.org.grassroot2.view.fragment.MeetingSingleInputFragment;

public class CreateActionActivity extends GrassrootActivity implements BackNavigationListener, CreateActionPresenter.CreateActionView {

    private static final String EXTRA_GROUP_UID = "groupUid";

    @BindView(R.id.viewPager) ViewPager               viewPager;
    @Inject                   CreateActionPresenter   presenter;

    private GenericViewPagerAdapter adapter;
    private boolean                 created;
    private boolean                 shouldRemoveLast;

    public static void start(Context c, String groupUid) {
        Intent i = new Intent(c, CreateActionActivity.class);
        i.putExtra(EXTRA_GROUP_UID, groupUid);
        c.startActivity(i);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_create_action;
    }

    public void nextClick() {
        int current = viewPager.getCurrentItem();
        if (current == adapter.getCount() - 1) {
            finish();
        } else {
            viewPager.setCurrentItem(current + 1, true);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        presenter.attach(this);
        presenter.verifyGroupPermissions(getIntent().getStringExtra(EXTRA_GROUP_UID));
    }

    @Override
    public void proceedWithRender(Group group) {
        adapter = new GenericViewPagerAdapter(getSupportFragmentManager());
        addActionTypeFragment(group);
        if (group == null) {
            addGroupSelectionFragment();
        } else {
            presenter.setGroupUid(group);
        }
        addMeetingSubjectFragment();
        addMeetingLocationFragment();
        addMeetingDateFragment();
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onInject(ActivityComponent component) {
        component.inject(this);
    }

    private void addMeetingDateFragment() {
        MeetingDateFragment meetingDateFragment = new MeetingDateFragment();
        disposables.add(meetingDateFragment.meetingDatePicked().subscribe(date -> {
            closeKeyboard();
            presenter.setMeetingDate(date);
            MeetingDateConfirmFragment f = MeetingDateConfirmFragment.newInstance(date);
            disposables.add(f.meetingDateConfirmed().subscribe(aLong -> presenter.createMeeting(), Throwable::printStackTrace));
            adapter.addFragment(f, "");
            nextClick();
            shouldRemoveLast = true;
        }));
        adapter.addFragment(meetingDateFragment, "");
    }

    private void addMeetingLocationFragment() {
        MeetingSingleInputFragment meetingSingleInputFragment = MeetingSingleInputFragment.newInstance(R.string.where_will_it_happen, R.string.info_meeting_location, R.string.hint_meeting_location);
        disposables.add(meetingSingleInputFragment.locationAdded().subscribe(location -> {
            presenter.setMeetingLocation(location);
            nextClick();
        }));
        adapter.addFragment(meetingSingleInputFragment, "");
        viewPager.setAdapter(adapter);
    }

    private void addMeetingSubjectFragment() {
        MeetingSingleInputFragment meetingSingleInputFragment = MeetingSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_meeting_subject, R.string.hint_meeting_subject);
        disposables.add(meetingSingleInputFragment.locationAdded().subscribe(subject -> {
            presenter.setSubject(subject);
            nextClick();
        }));
        adapter.addFragment(meetingSingleInputFragment, "");
        viewPager.setAdapter(adapter);
    }

    private void addGroupSelectionFragment() {
        GroupSelectionFragment fragment = new GroupSelectionFragment();
        disposables.add(fragment.itemSelection().subscribe(group -> {
            if (group.getPermissions().contains(GroupPermission.CREATE_GROUP_MEETING)) {
                presenter.setGroupUid(group);
                nextClick();
            } else {
                Snackbar.make(findViewById(android.R.id.content), R.string.error_permission_denied, Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }));
        adapter.addFragment(fragment, "");
    }

    private void addActionTypeFragment(Group group) {
        CreateActionFragment createActionFragment = CreateActionFragment.get(group);
        disposables.add(createActionFragment.clickAction().subscribe(integer -> {
            switch (integer) {
                case R.id.callMeeting:
                    nextClick();
                    break;
            }
        }));
        adapter.addFragment(createActionFragment, "");
    }

    @Override
    public void backPressed() {
        int current = viewPager.getCurrentItem();
        if (shouldRemoveLast) {
            adapter.removeLast();
            shouldRemoveLast = false;
        }
        if (current == 0 || created) {
            finish();
        } else {
            viewPager.setCurrentItem(current - 1, true);
        }
    }

    @Override
    public void backPressedAndRemoveLast() {
        shouldRemoveLast = false;
        backPressed();
        adapter.removeLast();
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach();
    }

    @Override
    public void uploadSuccessfull() {
        created = true;
        addMeetingCalledFragment();
    }

    @Override
    public void closeScreen() {
        finish();
    }

    private void addMeetingCalledFragment() {
        MeetingCalledFragment f = MeetingCalledFragment.get(presenter.getTask().getParentUid());
        adapter.addFragment(f, "");
        nextClick();
    }
}
