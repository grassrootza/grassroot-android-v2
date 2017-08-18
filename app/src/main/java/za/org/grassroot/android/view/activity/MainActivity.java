package za.org.grassroot.android.view.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import timber.log.Timber;
import za.org.grassroot.android.GrassrootApplication;
import za.org.grassroot.android.R;
import za.org.grassroot.android.dagger.activity.ActivityModule;
import za.org.grassroot.android.dagger.user.ApiModule;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.presenter.MainPresenter;
import za.org.grassroot.android.rxbinding.RxViewUtils;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.view.MainView;
import za.org.grassroot.android.view.fragment.SingleTextMultiButtonFragment;

public class MainActivity extends LoggedInActivity implements MainView {

    @Inject
    UserDetailsService userDetailsService;

    @Inject
    MainPresenter mainPresenter;

    BtnGrouping btnGrouping;
    SingleTextMultiButtonFragment mainFragment;

    MenuItem logoutItem;
    MenuItem syncItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((GrassrootApplication) getApplication())
                .getAppComponent()
                .plus(new ApiModule())
                .plus(new ActivityModule())
                .inject(this);
        setActivePresenter(mainPresenter);
        mainPresenter.attach(MainActivity.this);

        Bundle extras = getIntent().getExtras();
        btnGrouping = (extras == null) ? null : (BtnGrouping) extras.getParcelable(BtnGrouping.BUTTON_GROUP_DETAILS);
        if (btnGrouping == null) {
            btnGrouping = mainPresenter.obtainDefaultSubButtons();
        }

        mainFragment = SingleTextMultiButtonFragment.newInstance(
                R.string.main_explanation,
                false, -1, btnGrouping);
        Timber.d("Created main fragment, subscribing to view created");

        mainFragment.viewCreated().subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(@NonNull CharSequence sequence) throws Exception {
                Timber.d("Fragment view created, telling presenter to attach view");
                mainPresenter.onViewCreated();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frag_holder, mainFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home_screen, menu);
        logoutItem = menu.findItem(R.id.menu_logout);
        syncItem = menu.findItem(R.id.menu_sync);
        if (logoutItem != null && syncItem != null) {
            mainPresenter.menuReady();
        }
        return true;
    }

    // todo: pass it back to presenter
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                }
                break;
        }
    }

    @Override
    public void showProgressBar() {
        
    }

    @Override
    public void closeProgressBar() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUpActivity();
    }

    @Override
    public void cleanUpActivity() {
        mainPresenter.detach(this);
        mainPresenter.cleanUpForActivity();
    }

    @Override
    public void setBtnGrouping(BtnGrouping btnGrouping) {
        this.btnGrouping = btnGrouping; // and update views etc
    }

    @Override
    public Observable<BtnReturnBundle> threeButtonRowButtonClicked() {
        Timber.v("returning sub bar button clicked observables");
        return mainFragment.subtitleButtonClicked();
    }

    @Override
    public Observable<Boolean> logoutClicked() {
        return RxViewUtils.nullSafeMenuClick(logoutItem);
    }

    @Override
    public Observable<Boolean> syncTriggered() {
        return RxViewUtils.nullSafeMenuClick(syncItem);
    }
}

