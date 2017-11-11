package za.org.grassroot2.presenter.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.view.FragmentView;

/**
 * Created by qbasso on 21.09.2017.
 */

public class GroupSelectionPresenter extends BaseFragmentPresenter<GroupSelectionPresenter.GroupSelectionView> {

    private final DatabaseService databaseService;

    @Inject
    GroupSelectionPresenter(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void onViewCreated() {
        List<SelectableItem> data = new ArrayList<>();
        data.addAll(databaseService.loadGroupsSorted());
        getView().renderResults(data);
        disposableOnDetach(getView().searchChanged().debounce(500, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).flatMap(s -> Observable.just(databaseService.loadObjectsByName(Group.class, s))).observeOn(AndroidSchedulers.mainThread()).subscribe(groups -> {
            getView().renderResults(groups);
        }, Throwable::printStackTrace));
        disposableOnDetach(getView().groupClick().subscribe(s -> getView().groupSelected(s), Throwable::printStackTrace));
    }

    public interface GroupSelectionView extends FragmentView {
        void renderResults(List<? extends SelectableItem> data);
        Observable<String> searchChanged();
        Observable<Group> groupClick();
        void groupSelected(Group g);
    }
}
