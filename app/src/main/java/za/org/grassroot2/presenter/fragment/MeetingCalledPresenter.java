package za.org.grassroot2.presenter.fragment;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.account.SyncAdapter;
import za.org.grassroot2.view.FragmentView;

public class MeetingCalledPresenter extends BaseFragmentPresenter<MeetingCalledPresenter.MeetingCalledView> {

    private final DatabaseService    databaseService;

    @Inject
    public MeetingCalledPresenter(DatabaseService dbService) {
        databaseService = dbService;
    }

    public void loadGroupData(String groupUid) {
        disposableOnDetach(databaseService.load(Group.class, groupUid).subscribe(group -> view.showDescription(group.getMemberCount()), Throwable::printStackTrace));
    }

    @Override
    public void onViewCreated() {
    }

    public interface MeetingCalledView extends FragmentView{
        void showDescription(Integer memberCount);
    }
}
