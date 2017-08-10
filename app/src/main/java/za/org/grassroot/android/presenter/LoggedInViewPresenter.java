package za.org.grassroot.android.presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.view.LoginActivity;

/**
 * Created by luke on 2017/08/09.
 */
public abstract class LoggedInViewPresenter extends ViewPresenter {

    protected final UserDetailsService userDetailsService;

    public LoggedInViewPresenter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void logoutRetainingData() {
        userDetailsService.logoutRetainingData()
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

}
