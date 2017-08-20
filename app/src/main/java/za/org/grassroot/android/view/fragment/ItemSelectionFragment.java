package za.org.grassroot.android.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import timber.log.Timber;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.SelectableItem;
import za.org.grassroot.android.view.ItemSelectionView;
import za.org.grassroot.android.view.adapter.ItemSelectionAdapter;

public class ItemSelectionFragment<T extends RealmObject & SelectableItem> extends GrassrootFragment
        implements ItemSelectionView<T> {

    private static final String HEADER_STRING = "HEADER";

    @BindView(R.id.list_header) TextView listHeader;

    private ItemSelectionAdapter<T> recyclerViewAdapter;
    @BindView(R.id.list_recycler_view) RecyclerView recyclerView;

    public ItemSelectionFragment() {
        // mandatory
    }

    public static <T extends RealmObject & SelectableItem> ItemSelectionFragment<T> newInstance(int headerTextRes) {
        ItemSelectionFragment<T> fragment = new ItemSelectionFragment<>();
        Bundle args = new Bundle();
        args.putInt(HEADER_STRING, headerTextRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        lifecyclePublisher.onNext(ACTION_FRAGMENT_ATTACHED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selectable_item_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        if (getArguments() !=null && getArguments().containsKey(HEADER_STRING)) {
            listHeader.setVisibility(View.VISIBLE);
            listHeader.setText(getArguments().getInt(HEADER_STRING));
        } else {
            listHeader.setVisibility(View.GONE);
        }

        if (recyclerViewAdapter != null) {
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        } else {
            Timber.e("no adapter for recycler view during create view!");
        }

        lifecyclePublisher.onNext(ACTION_FRAGMENT_VIEW_CREATED);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Observable<Boolean> viewAttached() {
        return lifecyclePublisher
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer == ACTION_FRAGMENT_ATTACHED;
                    }
                })
                .map(new Function<Integer, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Integer integer) throws Exception {
                        return true;
                    }
                });
    }

    @Override
    public Observable<String> addData(OrderedRealmCollection<T> data) {
        recyclerViewAdapter = new ItemSelectionAdapter<T>(data, true, true);
        return recyclerViewAdapter.getViewClickObservable();
    }

}
