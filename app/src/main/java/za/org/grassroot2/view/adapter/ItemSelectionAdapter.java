package za.org.grassroot2.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.model.SelectableItem;


/**
 * Created by luke on 2017/08/19.
 */

public class ItemSelectionAdapter extends RecyclerView.Adapter<ItemSelectionAdapter.SelectableItemViewHolder> {

    private final List<SelectableItem> data;
    private PublishSubject<String> viewClickSubject = PublishSubject.create();

    public Observable<String> getViewClickObservable() {
        return viewClickSubject;
    }

    public ItemSelectionAdapter(List<SelectableItem> data) {
        super();
        this.data = data;
    }

    @Override
    public SelectableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_item_row, parent, false);
        return new SelectableItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SelectableItemViewHolder holder, int position) {
        SelectableItem item = data.get(position);
        if (item != null) {
            holder.itemHeading.setText(item.getName());
            holder.itemDescription.setText(item.getDescription());
            holder.itemUid = item.getUid();

            // todo: disposableOnDetach "takeUntil" detaches
            RxView.clicks(holder.itemRoot)
                    .map(o -> holder.itemUid)
                    .subscribe(viewClickSubject);
        } else {
            holder.itemHeading.setText("NULL");
            holder.itemDescription.setText("NULL");
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
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
