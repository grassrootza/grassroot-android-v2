package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.presenter.fragment.ItemSelectionFragmentPresenter;
import za.org.grassroot2.view.adapter.ItemSelectionAdapter;

public class ItemSelectionFragment extends GrassrootFragment implements ItemSelectionFragmentPresenter.ItemSelectionFragmentView {

    private static final String HEADER_STRING = "HEADER";

    @BindView(R.id.list_header) TextView listHeader;

    private ItemSelectionAdapter recyclerViewAdapter;
    @BindView(R.id.list_recycler_view) RecyclerView recyclerView;

    @Inject ItemSelectionFragmentPresenter presenter;

    public ItemSelectionFragment() {
    }

    public static ItemSelectionFragment newInstance(int headerTextRes) {
        ItemSelectionFragment fragment = new ItemSelectionFragment();
        Bundle args = new Bundle();
        args.putInt(HEADER_STRING, headerTextRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter.attach(this);
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (getArguments() !=null && getArguments().containsKey(HEADER_STRING)) {
            listHeader.setVisibility(View.VISIBLE);
            listHeader.setText(getArguments().getInt(HEADER_STRING));
        } else {
            listHeader.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.onViewCreated();
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_selectable_item_list;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.detach(this);
    }

    @Override
    public void renderResults(List<SelectableItem> data) {
        recyclerViewAdapter = new ItemSelectionAdapter(data);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        disposables.add(recyclerViewAdapter.getViewClickObservable().subscribe(s -> {
            EventBus.getDefault().post(new SelectionEvent(s));
        }));
    }

    public static class SelectionEvent {
        public final String s;

        SelectionEvent(String s) {
            this.s = s;
        }
    }
}
