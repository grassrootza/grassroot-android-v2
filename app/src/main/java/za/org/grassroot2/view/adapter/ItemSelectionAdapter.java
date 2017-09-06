package za.org.grassroot2.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import io.realm.RealmRecyclerViewAdapter;
import za.org.grassroot2.R;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.rxbinding.RxView;

/**
 * Created by luke on 2017/08/19.
 */

public class ItemSelectionAdapter<T extends RealmObject & SelectableItem> extends RealmRecyclerViewAdapter<T, ItemSelectionAdapter.SelectableItemViewHolder> {

    private PublishSubject<String> viewClickSubject = PublishSubject.create();

    public ItemSelectionAdapter(OrderedRealmCollection<T> data, boolean autoUpdate, boolean updateOnModification) {
        super(data, autoUpdate, updateOnModification);
    }

    public Observable<String> getViewClickObservable() {
        return viewClickSubject;
    }

    @Override
    public SelectableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_item_row, parent, false);
        return new SelectableItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SelectableItemViewHolder holder, int position) {
        SelectableItem item = getItem(position);
        if (item != null) {
            holder.itemHeading.setText(item.getName());
            holder.itemDescription.setText(item.getDescription());
            holder.itemUid = item.getUid();

            // todo: add "takeUntil" detaches
            RxView.clicks(holder.itemRoot)
                    .map(new Function<Object, String>() {
                        @Override
                        public String apply(@NonNull Object o) throws Exception {
                            return holder.itemUid;
                        }
                    })
                    .subscribe(viewClickSubject);
        } else {
            holder.itemHeading.setText("NULL");
            holder.itemDescription.setText("NULL");
        }
    }

    static class SelectableItemViewHolder extends RecyclerView.ViewHolder {

        ViewGroup itemRoot;
        TextView itemHeading;
        TextView itemDescription;
        String itemUid;

        private SelectableItemViewHolder(View itemView) {
            super(itemView);
            itemRoot = (ViewGroup) itemView.findViewById(R.id.item_root);
            itemHeading = (TextView) itemView.findViewById(R.id.item_heading);
            itemDescription = (TextView) itemView.findViewById(R.id.item_description);
        }
    }
}
