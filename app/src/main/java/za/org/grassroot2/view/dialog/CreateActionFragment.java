package za.org.grassroot2.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.util.GroupPermissionChecker;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.view.fragment.GrassrootFragment;

public class CreateActionFragment extends GrassrootFragment {

    private static final String EXTRA_GROUP = "group";
    @BindView(R.id.dictate) View dictate;
    @BindView(R.id.createTodo)  View createTodo;
    @BindView(R.id.takeVote)    View takeVote;
    @BindView(R.id.callMeeting) View callMeeting;

    private PublishSubject<Integer> actionSubject = PublishSubject.create();

    public CreateActionFragment() {
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_create_action;
    }

    public Observable<Integer> clickAction() {
        return actionSubject;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        List<Observable<Integer>> list = new ArrayList<>();
        list.add(RxView.clicks(dictate).map(o -> dictate.getId()));
        list.add(RxView.clicks(createTodo).map(o -> createTodo.getId()));
        list.add(RxView.clicks(takeVote).map(o -> takeVote.getId()));
        list.add(RxView.clicks(callMeeting).map(o -> callMeeting.getId()));
        Observable.merge(list).subscribe(actionSubject);
        v.findViewById(R.id.close).setOnClickListener(v1 -> {
            actionSubject.onComplete();
            getActivity().finish();
        });
        return v;
    }

    private void initView() {
        Group g = (Group) getArguments().getSerializable(EXTRA_GROUP);
        if (g!=null) {
            if (!GroupPermissionChecker.canCallMeeting(g)) {
                callMeeting.setVisibility(View.GONE);
            }
            if (!GroupPermissionChecker.canCreateTodo(g)) {
                createTodo.setVisibility(View.GONE);
            }
            if (!GroupPermissionChecker.canCreateVote(g)) {
                takeVote.setVisibility(View.GONE);
            }
        }
    }

    public static CreateActionFragment get(Group group) {
        CreateActionFragment f = new CreateActionFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_GROUP, group);
        f.setArguments(b);
        return f;
    }
}
