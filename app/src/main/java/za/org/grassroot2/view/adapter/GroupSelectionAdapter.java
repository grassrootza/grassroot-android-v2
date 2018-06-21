package za.org.grassroot2.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.SelectableItem;

/**
 * Created by luke on 2017/08/19.
 */

public class GroupSelectionAdapter extends RecyclerView.Adapter<GroupSelectionAdapter.SelectableItemViewHolder> {

    private List<? extends SelectableItem> data;
    private PublishSubject<Group> viewClickSubject = PublishSubject.create();

    public Observable<Group> getViewClickObservable() {
        return viewClickSubject;
    }

    public GroupSelectionAdapter(List<SelectableItem> data) {
        super();
        this.data = data;
    }

    @Override
    public SelectableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item_row, parent, false);
        return new SelectableItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SelectableItemViewHolder holder, int position) {
        SelectableItem item = data.get(position);
        holder.update(item);
        RxView.clicks(holder.name)
                .map(o -> (Group)item)
                .subscribe(viewClickSubject);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<? extends SelectableItem> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class SelectableItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text) TextView name;

        private SelectableItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void update(SelectableItem item) {
            name.setText(item.getName());
        }
    }
}
