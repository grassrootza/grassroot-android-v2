package za.org.grassroot.android.view.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import timber.log.Timber;
import za.org.grassroot.android.GrassrootApplication;
import za.org.grassroot.android.R;
import za.org.grassroot.android.dagger.activity.ActivityModule;
import za.org.grassroot.android.dagger.user.ApiModule;
import za.org.grassroot.android.model.SelectableItem;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.presenter.MainPresenter;
import za.org.grassroot.android.rxbinding.RxViewUtils;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.view.MainView;
import za.org.grassroot.android.view.fragment.GrassrootFragment;
import za.org.grassroot.android.view.fragment.ItemSelectionFragment;
import za.org.grassroot.android.view.fragment.LargeMsgWithButtonsFragment;
import za.org.grassroot.android.view.fragment.LongTextInputFragment;
import za.org.grassroot.android.view.fragment.SingleTextInputFragment;
import za.org.grassroot.android.view.fragment.SingleTextMultiButtonFragment;
import za.org.grassroot.android.view.fragment.TextInputFragment;

public class MainActivity extends LoggedInActivity implements MainView {

    @Inject
    UserDetailsService userDetailsService;

    @Inject
    MainPresenter mainPresenter;

    BtnGrouping btnGrouping;
    SingleTextMultiButtonFragment mainFragment;
    GrassrootFragment currentFragment;

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
            btnGrouping = mainPresenter.obtainMediaButtons();
        }

        mainFragment = SingleTextMultiButtonFragment.newInstance(
                R.string.main_explanation,
                false, -1, btnGrouping);
        Timber.d("Created main fragment, subscribing to view created");

        mainFragment.viewCreated().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                Timber.d("Fragment view created, telling presenter to attach view");
                mainPresenter.onViewCreated();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frag_holder, mainFragment)
                .commit();

        currentFragment = mainFragment;
    }

    // todo : watch out for the use of this 'MainFragment', may cause issues (quite a few)
    // todo : double check if clear back stack needed
    @Override
    public Observable<BtnReturnBundle> defaultRequestTextOrButtons(int headerString, Integer explanationRes, boolean insideCreateCycle) {
        Timber.e("calling default request text or buttons");
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (!insideCreateCycle) {
            Timber.e("popping back stack and recreating");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mainFragment = SingleTextMultiButtonFragment.newInstance(
                    headerString,
                    explanationRes != null,
                    explanationRes != null ? explanationRes : -1,
                    btnGrouping);
        }


        Observable<BtnReturnBundle> observable = mainFragment
                .viewCreated()
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable);
                    }
                })
                .concatMap(new Function<Integer, ObservableSource<? extends BtnReturnBundle>>() {
                    @Override
                    public ObservableSource<? extends BtnReturnBundle> apply(@NonNull Integer integer) throws Exception {
                        Timber.e("returning main fragment next");
                        mainFragment.focusOnInput();
                        return mainFragment.mainTextNext();
                    }
                });

        if (!insideCreateCycle) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_frag_holder, mainFragment);
            transaction.commit();
        }

        currentFragment = mainFragment;
        return observable;
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

    // todo : handle / pass back the 'cancel' (might need a simple DTO)
    @Override
    public Observable<CharSequence> requestTextInputNextCancel(int headerString, int explanationRes) {
        final SingleTextInputFragment fragment = SingleTextInputFragment.newInstance(headerString, explanationRes,
                R.string.button_next, android.R.string.cancel);
        Observable<CharSequence> observable = fragment
                .viewCreated()
                .concatMap(new Function<Integer, Observable<CharSequence>>() {
                    @Override
                    public Observable<CharSequence> apply(@NonNull Integer integer) throws Exception {
                        return fragment.textInputNextDone();
                    }
                });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frag_holder, fragment)
                .addToBackStack("TEXT_ENTRY")
                .commit();

        currentFragment = fragment;
        return observable;
    }

    @Override
    public Observable<Integer> requestLongDescriptionInput(int headerString, final int inputHint) {
        final LongTextInputFragment longTextFragment = LongTextInputFragment.newInstance(headerString, inputHint,
                R.string.button_skip, R.string.button_next);

        Observable<Integer> observable = longTextFragment
                .viewCreated()
                .concatMap(new Function<Integer, ObservableSource<? extends Integer>>() {
                    @Override
                    public ObservableSource<? extends Integer> apply(@NonNull Integer integer) throws Exception {
                        longTextFragment.focusOnInput();
                        return Observable.just(integer);
                    }
                });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frag_holder, longTextFragment)
                .addToBackStack("LONG_DESCRIPTION")
                .commit();

        currentFragment = longTextFragment;
        return observable;
    }

    @Override
    public <T extends RealmObject & SelectableItem> Observable<String> requestSelection(int headerRes,
                                                                                        final OrderedRealmCollection<T> items) {
        final ItemSelectionFragment<T> selectionFragment = ItemSelectionFragment.newInstance(headerRes);
        Observable<String> observable = selectionFragment
                .viewAttached()
                .concatMap(new Function<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> apply(@NonNull Boolean aBoolean) throws Exception {
                        return selectionFragment.addData(items);
                    }
                });

        Timber.d("created observable, about to commit fragment");
        closeKeyboard();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frag_holder, selectionFragment)
                .addToBackStack("SELECTION")
                .commit();

        Timber.d("fragment committed, returning observable");
        return observable;
    }

    @Override
    public Observable<Integer> requestConfirmationOrAction(int headerRes, String message, BtnGrouping btnGrouping, boolean allowSkip) {
        final LargeMsgWithButtonsFragment fragment = LargeMsgWithButtonsFragment.newInstance(headerRes,
                message, true, btnGrouping, allowSkip);

        /* Observable<BtnReturnBundle> observable = fragment.viewCreated()
                .concatMap(new Function<Integer, ObservableSource<? extends BtnReturnBundle>>() {
                    @Override
                    public ObservableSource<? extends BtnReturnBundle> apply(@NonNull Integer integer) throws Exception {
                        return fragment.buttonClicked();
                    }
                });*/

        closeProgressBar();
        closeKeyboard();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frag_holder, fragment)
                .addToBackStack("CONFIRMATION")
                .commit();

        currentFragment = fragment;
        return fragment.viewCreated();
    }

    @Override
    public Observable<BtnReturnBundle> btnGroupClicked() {
        Timber.e("returning observable of this thing");
        return currentFragment instanceof LargeMsgWithButtonsFragment ?
                ((LargeMsgWithButtonsFragment) currentFragment).buttonClicked() :
                Observable.<BtnReturnBundle>empty();
    }

    @Override
    public Observable<Object> skipButtonClicked() {
        if (currentFragment instanceof LargeMsgWithButtonsFragment) {
            return ((LargeMsgWithButtonsFragment) currentFragment).skipClicked();
        } else if (currentFragment instanceof LongTextInputFragment) {
            return ((LongTextInputFragment) currentFragment).skipClicked();
        } else {
            return Observable.empty();
        }
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
    public Observable<BtnReturnBundle> mainTextNext() {
        if (currentFragment instanceof TextInputFragment) {
            return ((TextInputFragment) currentFragment).mainTextNext();
        } else {
            return Observable.empty();
        }
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

