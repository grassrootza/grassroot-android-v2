package za.org.grassroot2.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;

import com.tbruyelle.rxpermissions2.RxPermissions;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.GroupPermission;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.presenter.CreateActionPresenter;
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter;
import za.org.grassroot2.view.dialog.MediaPickerFragment;
import za.org.grassroot2.view.dialog.MultiOptionPickFragment;
import za.org.grassroot2.view.fragment.ActionSingleInputFragment;
import za.org.grassroot2.view.fragment.BackNavigationListener;
import za.org.grassroot2.view.fragment.GroupSelectionFragment;
import za.org.grassroot2.view.fragment.ItemCreatedFragment;
import za.org.grassroot2.view.fragment.LivewireConfirmFragment;
import za.org.grassroot2.view.fragment.MeetingDateConfirmFragment;
import za.org.grassroot2.view.fragment.MeetingDateFragment;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class CreateActionActivity extends GrassrootActivity implements BackNavigationListener, CreateActionPresenter.CreateActionView {

    private static final String EXTRA_GROUP_UID      = "groupUid";
    private static final int    REQUEST_TAKE_PHOTO   = 1;
    private static final int    REQUEST_RECORD_VIDEO = 2;
    private static final int    REQUEST_GALLERY      = 3;

    @BindView(R.id.viewPager) ViewPager             viewPager;
    @Inject                   CreateActionPresenter presenter;
    @Inject                   RxPermissions         rxPermission;

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

    public void nextStep() {
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
        viewPager.setAdapter(adapter);
    }

    @Override
    public void cameraForResult(String contentProviderPath, String s) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath));
        cameraIntent.putExtra("MY_UID", s);
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    public void videoForResult(String contentProviderPath, String s) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath));
        cameraIntent.putExtra("MY_UID", s);
        startActivityForResult(cameraIntent, REQUEST_RECORD_VIDEO);
    }

    @Override
    public void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
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
            nextStep();
            shouldRemoveLast = true;
        }));
        adapter.addFragment(meetingDateFragment, "");
    }

    private void addMeetingLocationFragment() {
        ActionSingleInputFragment actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.where_will_it_happen, R.string.info_meeting_location, R.string.hint_meeting_location, false);
        disposables.add(actionSingleInputFragment.inputAdded().subscribe(location -> {
            presenter.setMeetingLocation(location);
            nextStep();
        }));
        adapter.addFragment(actionSingleInputFragment, "");
    }

    private void addMeetingSubjectFragment() {
        ActionSingleInputFragment actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_meeting_subject, R.string.hint_meeting_subject, false);
        disposables.add(actionSingleInputFragment.inputAdded().subscribe(subject -> {
            presenter.setSubject(subject);
            nextStep();
        }));
        adapter.addFragment(actionSingleInputFragment, "");
    }

    private void addHeadlineFragment() {
        ActionSingleInputFragment actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_headline, R.string.hint_headline, false);
        disposables.add(actionSingleInputFragment.inputAdded().subscribe(headline -> {
            presenter.setHeadline(headline);
            nextStep();
        }));
        adapter.addFragment(actionSingleInputFragment, "");
    }

    private void addLongDescriptionFragment() {
        ActionSingleInputFragment actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.long_description, R.string.info_long_description, R.string.hint_description, true);
        actionSingleInputFragment.setMultiLine(true);
        disposables.add(actionSingleInputFragment.inputAdded().flatMapMaybe(description -> {
            presenter.setLongDescription(description);
            return presenter.getAlertAndGroupName();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(entry -> {
            LivewireConfirmFragment f = LivewireConfirmFragment.newInstance(entry.getValue(), entry.getKey());
            adapter.addFragment(f, "");
            disposables.add(f.livewireAlertConfirmed().subscribe(aLong -> presenter.createAlert(), Throwable::printStackTrace));
            nextStep();
            shouldRemoveLast = true;
        }, Throwable::printStackTrace));
        adapter.addFragment(actionSingleInputFragment, "");
    }

    private void addGroupSelectionFragment() {
        GroupSelectionFragment fragment = new GroupSelectionFragment();
        disposables.add(fragment.itemSelection().subscribe(group -> {
            if (group.getPermissions().contains(GroupPermission.CREATE_GROUP_MEETING)) {
                presenter.setGroupUid(group);
                nextStep();
            } else {
                Snackbar.make(findViewById(android.R.id.content), R.string.error_permission_denied, Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }));
        adapter.addFragment(fragment, "");
    }

    @Override
    public Observable<Boolean> ensureWriteExteralStoragePermission() {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void addActionTypeFragment(Group group) {
        MultiOptionPickFragment createActionFragment = MultiOptionPickFragment.getActionPicker(group);
        disposables.add(createActionFragment.clickAction().subscribe(integer -> {
            switch (integer) {
                case R.id.callMeeting:
                    removeAllViewsAboveCurrent();
                    presenter.initTask(CreateActionPresenter.ActionType.Meeting);
                    if (group == null) {
                        addGroupSelectionFragment();
                    } else {
                        presenter.setGroupUid(group);
                    }
                    addMeetingSubjectFragment();
                    addMeetingLocationFragment();
                    addMeetingDateFragment();
                    nextStep();
                    break;
                case R.id.createLivewireAlert:
                    removeAllViewsAboveCurrent();
                    presenter.initTask(CreateActionPresenter.ActionType.LivewireAlert);
                    if (group == null) {
                        addGroupSelectionFragment();
                    } else {
                        presenter.setGroupUid(group);
                    }
                    addHeadlineFragment();
                    addMediaFragment();
                    addLongDescriptionFragment();
                    nextStep();
                    break;
            }
        }));
        adapter.addFragment(createActionFragment, "");
    }

    private void removeAllViewsAboveCurrent() {
        int current = viewPager.getCurrentItem();
        adapter.removeAllAbove(current);
    }

    private void addMediaFragment() {
        MediaPickerFragment mediaPickerFragment = MediaPickerFragment.getMediaPicker();
        disposables.add(mediaPickerFragment.clickAction().subscribe(integer -> {
            switch (integer) {
                case R.id.photo:
                    presenter.takePhoto();
                    break;
                case R.id.video:
                    presenter.recordVideo();
                    break;
                case R.id.gallery:
                    presenter.pickFromGallery();
                    break;
                case R.id.skip:
                    nextStep();
                    break;
            }
        }));
        adapter.addFragment(mediaPickerFragment, "");
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
    public void uploadSuccessfull(GrassrootEntityType type) {
        created = true;
        addSuccessFragment(type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                presenter.cameraResult();
            } else {
                presenter.handlePickResult(data.getData());
            }
            nextStep();
        }
    }

    @Override
    public void closeScreen() {
        finish();
    }

    private void addSuccessFragment(GrassrootEntityType type) {
        ItemCreatedFragment f;
        if (type == GrassrootEntityType.MEETING) {
            f = ItemCreatedFragment.get(presenter.getTask().getParentUid(), type);
        } else {
            f = ItemCreatedFragment.get(presenter.getAlert().getGroupUid(), type);
        }
        adapter.addFragment(f, "");
        nextStep();
    }

}
