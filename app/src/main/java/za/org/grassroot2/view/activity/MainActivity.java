package za.org.grassroot2.view.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.Observable;
import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.user.ApiModule;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.presenter.MainPresenter;
import za.org.grassroot2.rxbinding.RxViewUtils;
import za.org.grassroot2.view.MainView;
import za.org.grassroot2.view.event.MoveNextWithInputEvent;
import za.org.grassroot2.view.fragment.GrassrootFragment;
import za.org.grassroot2.view.fragment.ItemSelectionFragment;
import za.org.grassroot2.view.fragment.LargeMsgWithButtonsFragment;
import za.org.grassroot2.view.fragment.LongTextInputFragment;
import za.org.grassroot2.view.fragment.SingleTextInputFragment;
import za.org.grassroot2.view.fragment.SingleTextMultiButtonFragment;

public class MainActivity extends LoggedInActivity implements MainView {

    @Inject
    MainPresenter presenter;

    @Inject
    RxPermissions rxPermissions;

    BtnGrouping btnGrouping;
    MenuItem    logoutItem;
    MenuItem    syncItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ((GrassrootApplication) getApplication())
                .getAppComponent().plus(getActivityModule()).inject(this);

        setActivePresenter(presenter);
        presenter.attach(MainActivity.this);

        Bundle extras = getIntent().getExtras();
        btnGrouping = (extras == null) ? null : (BtnGrouping) extras.getParcelable(BtnGrouping.BUTTON_GROUP_DETAILS);
        if (btnGrouping == null) {
            btnGrouping = presenter.obtainMediaButtons();
        }
        SingleTextMultiButtonFragment fragment = SingleTextMultiButtonFragment.newInstance(
                R.string.main_explanation,
                false, -1, null);
        Timber.d("Created main fragment, subscribing to view created");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frag_holder, fragment)
                .commit();
        DashboardActivity.start(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home_screen, menu);
        logoutItem = menu.findItem(R.id.menu_logout);
        syncItem = menu.findItem(R.id.menu_sync);
        if (logoutItem != null && syncItem != null) {
            presenter.menuReady();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
    }

    @Override
    public Observable<Boolean> ensureExternalStoragePermission() {
        return rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public Observable<Boolean> ensureAudioRecordingPermissions() {
        return rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
    }

    @Override
    public void askForHeadline() {
        final SingleTextInputFragment fragment = SingleTextInputFragment.newInstance(R.string.lwire_headline_title, R.string.lwire_headline_explan,
                R.string.button_next, android.R.string.cancel);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frag_holder, fragment)
                .addToBackStack("TEXT_ENTRY")
                .commit();
    }

    @Override
    public void askForMediaFile() {
        final LargeMsgWithButtonsFragment fragment = LargeMsgWithButtonsFragment.newInstance(R.string.lwire_media_header,
                getString(R.string.lwire_media_prompt), true,
                presenter.obtainMediaButtons(), true);
        closeProgressBar();
        closeKeyboard();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frag_holder, fragment)
                .addToBackStack("MEDIA")
                .commit();
    }

    @Override
    public void loadGroupSelection() {
        final ItemSelectionFragment selectionFragment = ItemSelectionFragment.newInstance(R.string.group_select_title);
        closeKeyboard();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frag_holder, selectionFragment)
                .addToBackStack("SELECTION")
                .commit();
    }

    @Override
    public void askForDescription() {
        final LongTextInputFragment longTextFragment = LongTextInputFragment.newInstance(R.string.lwire_description_header, R.string.lwire_description_hint,
                R.string.button_skip, R.string.button_next);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frag_holder, longTextFragment)
                .addToBackStack("LONG_DESCRIPTION")
                .commit();
    }

    @Override
    public void askForConfirmation() {
        final LargeMsgWithButtonsFragment fragment = LargeMsgWithButtonsFragment.newInstance(R.string.lwire_media_header,
                presenter.alertConfirmBody(), true,
                presenter.getConfirmButtons(), false);
        closeProgressBar();
        closeKeyboard();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frag_holder, fragment)
                .addToBackStack("CONFIRMATION")
                .commit();
    }

    @Override
    public void goToDefaultScreen() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = count; i > 0; i--) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void cameraForResult(Uri output, String s, int requestCode) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        cameraIntent.putExtra("MY_UID", s);
        startActivityForResult(cameraIntent, requestCode);
    }

    @Override
    public Observable<Boolean> logoutClicked() {
        return RxViewUtils.nullSafeMenuClick(logoutItem);
    }

    @Override
    public Observable<Boolean> syncTriggered() {
        return RxViewUtils.nullSafeMenuClick(syncItem);
    }

    @Subscribe
    public void singleInput(SingleTextInputFragment.SingleInputTextEvent e) {
    }

    @Subscribe
    public void gotHeadline(MoveNextWithInputEvent e) {
//        presenter.createOrUpdateLiveWireAlertWithHeadline(e.input);
    }

    @Subscribe
    public void moveNextWithSingleInput(MoveNextWithInputEvent e) {
        presenter.createOrUpdateLiveWireAlertWithHeadline(e.input);
    }

    @Subscribe
    public void mediaButtonClick(LargeMsgWithButtonsFragment.BtnClickEvent e) {
        if (e.value == null) {
            presenter.skipMedia();
        } else {
            presenter.handleMediaButtonClick(e.value);
        }
    }

    @Subscribe
    public void groupSelected(ItemSelectionFragment.SelectionEvent e) {
        presenter.setGroupForAlert(e.s);
    }

    @Subscribe
    public void gotDescription(LongTextInputFragment.LongInputEvent e) {
        presenter.setDescription(e.s);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }
}

