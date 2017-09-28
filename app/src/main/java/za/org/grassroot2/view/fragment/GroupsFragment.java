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
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter;
import za.org.grassroot2.view.activity.GrassrootActivity;
import za.org.grassroot2.view.adapter.GroupsAdapter;

public class GroupsFragment extends GrassrootFragment implements GroupFragmentPresenter.GroupFragmentView {

    @Inject                            GroupFragmentPresenter presenter;
    @BindView(R.id.toolbar)            Toolbar                toolbar;
    @BindView(R.id.emptyInfoContainer) View                   emptyInfoContainer;
    @BindView(R.id.emptyInfo)          TextView               emptyInfo;
    @BindView(R.id.groupRecyclerView)  RecyclerView           groupsRecyclerView;

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
        presenter.attach(this);
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
        presenter.onViewCreated();
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_groups;
    }

    @Override
    protected void onInject(GrassrootApplication application) {
        application.getAppComponent().plus(((GrassrootActivity) getActivity()).getActivityModule()).inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detach(this);
    }

    @Override
    public void render(List<Group> groups) {
        emptyInfoContainer.setVisibility(View.GONE);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        GroupsAdapter adapter = new GroupsAdapter(getActivity(), groups);
        View footer = LayoutInflater.from(getActivity()).inflate(R.layout.item_group_footer, null, false);
        adapter.addFooter(footer);
        groupsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void renderEmpty() {
        displayEmptyLayout();
        emptyInfo.setText(R.string.no_group_info);
    }

    @Override
    public void renderEmptyFailedSync() {
        displayEmptyLayout();
        emptyInfo.setText(R.string.sync_problem);
    }

    @OnClick(R.id.fab)
    public void fabClick() {

    }

    private void displayEmptyLayout() {
        emptyInfoContainer.setVisibility(View.VISIBLE);
        groupsRecyclerView.setVisibility(View.GONE);
    }
}
