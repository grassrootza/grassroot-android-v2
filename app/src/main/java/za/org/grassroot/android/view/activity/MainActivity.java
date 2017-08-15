package za.org.grassroot.android.view.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;

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

        Bundle extras = getIntent().getExtras();
        btnGrouping = (extras == null) ? null : (BtnGrouping) extras.getParcelable(BtnGrouping.BUTTON_GROUP_DETAILS);
        if (btnGrouping == null) {
            btnGrouping = mainPresenter.obtainDefaultSubButtons();
        }

        mainFragment = SingleTextMultiButtonFragment.newInstance(
                R.string.main_explanation,
                false, -1, btnGrouping);
        Timber.e("Created main fragment, subscribing to view created");
        mainFragment.viewCreated().subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(@NonNull CharSequence sequence) throws Exception {
                Timber.e("Fragment view created, telling presenter to attach view");
                mainPresenter.attach(MainActivity.this);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frag_holder, mainFragment)
                .commit();
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
    public Observable<BtnReturnBundle> subBarButtonClicked() {
        Timber.v("returning sub bar button clicked observables");
        return mainFragment.subtitleButtonClicked();
    }
}

