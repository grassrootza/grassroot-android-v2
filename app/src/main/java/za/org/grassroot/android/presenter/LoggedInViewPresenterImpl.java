package za.org.grassroot.android.presenter;

import android.content.Intent;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.view.LoginActivity;

/**
 * Created by luke on 2017/08/09.
 */
public abstract class LoggedInViewPresenterImpl extends ViewPresenterImpl implements LoggedInViewPresenter {

    protected final UserDetailsService userDetailsService;

    public LoggedInViewPresenterImpl(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public abstract void handleActivityResult(int requestCode, Intent data);

    public abstract void handleActivityResultError(int requestCode, int resultCode, Intent data);

    public void logoutRetainingData() {
        userDetailsService.logout(false, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            view.launchActivity(LoginActivity.class, null);
                        }
                    }
                });
    }

    @Override
    public void triggerAccountSync() {
        userDetailsService.requestSync();
    }

}
