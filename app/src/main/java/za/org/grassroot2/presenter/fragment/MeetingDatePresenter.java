package za.org.grassroot2.presenter.fragment;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.FragmentView;

/**
 * Created by qbasso on 21.09.2017.
 */

public class MeetingDatePresenter extends BaseFragmentPresenter<MeetingDatePresenter.MeetingDateView> {

    private final NetworkService networkService;

    @Inject
    MeetingDatePresenter(NetworkService networkService) {
        this.networkService = networkService;
    }

    @Override
    public void onViewCreated() {
        disposableOnDetach(view.dateInputConfirmed().observeOn(AndroidSchedulers.mainThread()).flatMap(s -> {
            view.showProgressBar();
            return networkService.getTimestampForText(s);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(timestamp -> view.renderDate(timestamp), throwable -> {
            throwable.printStackTrace();
            view.closeProgressBar();
            view.showDatePicker();
        }));
    }

    public interface MeetingDateView extends FragmentView {
        Observable<String> dateInputConfirmed();
        void renderDate(Long timestamp);
        void showDatePicker();
    }
}
