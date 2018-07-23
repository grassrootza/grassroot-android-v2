package za.org.grassroot2.view.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.fragment.FragmentComponent;
import za.org.grassroot2.dagger.fragment.FragmentModule;
import za.org.grassroot2.services.OfflineReceiver;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.util.AlarmManagerHelper;
import za.org.grassroot2.util.UserPreference;
import za.org.grassroot2.util.ViewAnimation;
import za.org.grassroot2.view.FragmentView;
import za.org.grassroot2.view.activity.GrassrootActivity;
import za.org.grassroot2.view.dialog.GenericErrorDialog;
import za.org.grassroot2.view.dialog.NoConnectionDialog;

/**
 * Created by luke on 2017/08/10.
 */

public abstract class GrassrootFragment extends Fragment implements FragmentView {

    protected static final int ACTION_FRAGMENT_CREATED      = 2;
    protected static final int ACTION_FRAGMENT_VIEW_CREATED = 3;
    private static final String DIALOG_TAG                  = "dialog";

    protected Unbinder unbinder;
    protected PublishSubject<Integer> lifecyclePublisher = PublishSubject.create();
    protected CompositeDisposable disposables = new CompositeDisposable();

    View progress;
    @Inject        UserPreference       userPreference;
    @Inject public Lazy<AccountManager> accountManagerProvider;
    private FragmentComponent component;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        GrassrootApplication application = (GrassrootApplication) getActivity().getApplication();
        ActivityComponent activityComponent = application.getAppComponent().plus(((GrassrootActivity) getActivity()).getActivityModule());
        activityComponent.inject(this);
        onInject(activityComponent);
    }

    protected abstract void onInject(ActivityComponent activityComponent);

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        Timber.i("inside GrassrootFragment onDestroyView");
        disposables.clear();
        component = null;
        if (this.unbinder != null) {
            unbinder.unbind();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getLayoutResourceId() , container, false);
        unbinder = ButterKnife.bind(this, v);
        progress = getActivity().findViewById(R.id.progress);
        return v;
    }

    @LayoutRes
    public abstract int getLayoutResourceId();


    @Override
    public Observable<Integer> viewCreated() {
        return lifecyclePublisher
                .filter(integer -> integer == ACTION_FRAGMENT_VIEW_CREATED);
    }

    @Override
    public void showNoConnectionMessage() {
        ((GrassrootActivity) getActivity()).showNoConnectionMessage();
    }


    public void showErrorDialog(int errorMsgResId) {
        DialogFragment dialog = GenericErrorDialog.Companion.newInstance(errorMsgResId);
        dialog.show(getChildFragmentManager(), DIALOG_TAG);
    }



    @Override
    public void handleNoConnection() {
        if (!userPreference.connectionInfoDisplayed()) {
            DialogFragment dialog;
            if (loggedIn()) {
                dialog = NoConnectionDialog.Companion.newInstance(NoConnectionDialog.TYPE_AUTHORIZED);
            } else {
                dialog = NoConnectionDialog.Companion.newInstance(NoConnectionDialog.TYPE_NOT_AUTHORIZED);
            }
            dialog.show(getChildFragmentManager(), DIALOG_TAG);
            userPreference.setNoConnectionInfoDisplayed(true);
            AlarmManagerHelper.scheduleAlarmForBroadcastReceiver(getActivity(), OfflineReceiver.class);
        }
    }

    @Override
    public void handleNoConnectionUpload() {
        if (userPreference.connectionInfoDisplayed()) {
            showNoConnectionMessage();
        } else {
            handleNoConnection();
        }
    }

    protected boolean loggedIn() {
        Account[] accounts = accountManagerProvider.get().getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length != 0 && !TextUtils.isEmpty(accountManagerProvider.get().getUserData(accounts[0], AuthConstants.USER_DATA_LOGGED_IN));
    }

    @Override
    public void showProgressBar() {
        Timber.d("showing progress bar in fragment");
        if (progress!=null) {
            ViewAnimation.fadeIn(progress);
        }
    }

    @Override
    public void closeProgressBar() {
        if (progress!=null) {
            ViewAnimation.fadeOut(progress);
        }
    }

    protected FragmentComponent get() {
        if (component == null) {
            component = ((GrassrootActivity)getActivity()).getComponenet().plus(new FragmentModule());
        }
        return component;
    }
}
