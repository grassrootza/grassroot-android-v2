package za.org.grassroot2.presenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.view.GrassrootView;


public class GroupDetailsPresenter extends BasePresenter<GroupDetailsPresenter.GroupDetailsView>{

    @Inject
    public GroupDetailsPresenter(DatabaseService service) {
        databaseService = service;
    }

    private DatabaseService databaseService;

    public void loadGroup(String groupUid) {
        databaseService.load(Group.class, groupUid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(group -> {
            if (view != null) {
                view.render(group);
            }
        }, Throwable::printStackTrace);
    }

    public interface GroupDetailsView extends GrassrootView {
        void render(Group group);
    }
}
