package za.org.grassroot2.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.dagger.fragment.FragmentComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter;
import za.org.grassroot2.view.activity.CreateActionActivity;
import za.org.grassroot2.view.activity.GroupDetailsActivity;
import za.org.grassroot2.view.adapter.GroupsAdapter;

public class GroupsFragment extends GrassrootFragment implements GroupFragmentPresenter.GroupFragmentView {

    @Inject                            GroupFragmentPresenter presenter;
    @BindView(R.id.toolbar)            Toolbar                toolbar;
    @BindView(R.id.emptyInfoContainer) View                   emptyInfoContainer;
    @BindView(R.id.emptyInfo)          TextView               emptyInfo;
    @BindView(R.id.groupRecyclerView)  RecyclerView           groupsRecyclerView;
    private GroupsAdapter                                     groupsAdapter;

    public GroupsFragment() {
    }

    public static GroupsFragment newInstance() {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_groups, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_groups);
        presenter.attach(this);
        presenter.onViewCreated();
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_groups;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detach(this);
    }

    @Override
    public void render(List<Group> groups) {
        groupsRecyclerView.setVisibility(View.VISIBLE);
        emptyInfoContainer.setVisibility(View.GONE);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupsAdapter = new GroupsAdapter(getActivity(), groups);
        View footer = LayoutInflater.from(getActivity()).inflate(R.layout.item_group_footer, null, false);
        groupsAdapter.addFooter(footer);
        groupsRecyclerView.setAdapter(groupsAdapter);
    }

    @Override
    public void renderEmpty() {
        displayEmptyLayout();
        emptyInfo.setText(R.string.no_group_info);
    }

    @Override
    public Observable<String> itemClick() {
        return groupsAdapter.getViewClickObservable();
    }

    @Override
    public void renderEmptyFailedSync() {
        displayEmptyLayout();
        emptyInfo.setText(R.string.sync_problem);
    }

    @Override
    public void openDetails(String groupUid) {
        GroupDetailsActivity.start(getActivity(), groupUid);
    }

    @OnClick(R.id.fab)
    public void fabClick() {
        CreateActionActivity.start(getActivity(), null);
    }

    private void displayEmptyLayout() {
        emptyInfoContainer.setVisibility(View.VISIBLE);
        groupsRecyclerView.setVisibility(View.GONE);
    }

}
