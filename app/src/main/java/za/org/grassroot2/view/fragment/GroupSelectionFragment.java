package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.presenter.fragment.GroupSelectionPresenter;
import za.org.grassroot2.rxbinding.RxTextView;
import za.org.grassroot2.view.adapter.GroupSelectionAdapter;

/**
 * Created by qbasso on 18.10.2017.
 */

public class GroupSelectionFragment extends GrassrootFragment implements GroupSelectionPresenter.GroupSelectionView {

    @BindView(R.id.groupRecyclerView) RecyclerView recyclerView;
    @BindView(R.id.close)             View         close;
    @BindView(R.id.searchInput)       EditText     searchInput;
    private PublishSubject<Group> itemSelectedSubject = PublishSubject.create();

    @Inject GroupSelectionPresenter presenter;
    private GroupSelectionAdapter   adapter;

    private BackNavigationListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (BackNavigationListener) getActivity();
    }

    @OnClick(R.id.backNav)
    void back() {
        listener.backPressed();
    }

    @OnClick(R.id.close)
    void close() {
        getActivity().finish();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.attach(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GroupSelectionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        presenter.onViewCreated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_group_selection;
    }

    @Override
    public void renderResults(List<? extends SelectableItem> data) {
        adapter.updateData(data);
    }

    @Override
    public Observable<String> searchChanged() {
        return RxTextView.textChanges(searchInput).map(CharSequence::toString);
    }

    public Observable<Group> itemSelection() {
        return itemSelectedSubject;
    }

    @Override
    public Observable<Group> groupClick() {
        return adapter.getViewClickObservable();
    }

    @Override
    public void groupSelected(Group g) {
        itemSelectedSubject.onNext(g);
    }
}
