package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.presenter.fragment.GroupTasksPresenter;
import za.org.grassroot2.view.activity.GrassrootActivity;
import za.org.grassroot2.view.adapter.GroupTasksAdapter;
import za.org.grassroot2.view.adapter.HeaderItem;

public class GroupTasksFragment extends GrassrootFragment implements GroupTasksPresenter.AllFragmentView {

    private static final String EXTRA_GROUP_UID = "groupUid";
    private static final String EXTRA_TYPE      = "type";

    private long oldAfter;


    @Inject                         GroupTasksPresenter presenter;
    @BindView(R.id.allRecyclerView) RecyclerView        allRecyclerView;
    @BindView(R.id.emptyInfo)       TextView            emptyInfo;
    private                         GroupTasksAdapter   adapter;

    public static Fragment newInstance(String groupUid, GrassrootEntityType type) {
        GroupTasksFragment allFragment = new GroupTasksFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_GROUP_UID, groupUid);
        b.putSerializable(EXTRA_TYPE, type);
        allFragment.setArguments(b);
        return allFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        presenter.attach(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
    }

    @Override
    protected void onInject(GrassrootApplication application) {
        application.getAppComponent().plus(((GrassrootActivity) getActivity()).getActivityModule()).inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_group_items;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        oldAfter = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        allRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GroupTasksAdapter(getActivity(), new ArrayList<>(), oldAfter);
        allRecyclerView.setAdapter(adapter);
        presenter.init(getArguments().getString(EXTRA_GROUP_UID), (GrassrootEntityType) getArguments().getSerializable(EXTRA_TYPE));
        presenter.loadTasks();
    }

    @Override
    public void render(List<Task> tasks) {
        emptyInfo.setVisibility(View.GONE);
        adapter.setData(tasks);
    }

    @Override
    public void empty() {
        emptyInfo.setVisibility(View.VISIBLE);
    }
}
