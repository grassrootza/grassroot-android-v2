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
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.model.dto.ActionOption;

/**
 * Created by qbasso on 25.10.2017.
 */
public class OptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ActionOption> data;
    private PublishSubject<Integer> viewClickSubject = PublishSubject.create();

    public Observable<Integer> getViewClickObservable() {
        return viewClickSubject;
    }

    public OptionAdapter(List<ActionOption> data) {
        super();
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.e("creating view holder ...");
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ActionOption item = data.get(position);
        OptionViewHolder h = (OptionViewHolder) holder;
        if (item != null) {
            h.text.setText(item.textId);
            h.text.setCompoundDrawablesWithIntrinsicBounds(item.resId, 0, 0, 0);
            RxView.clicks(h.text)
                    .map(o -> item.id)
                    .subscribe(viewClickSubject);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class OptionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text) TextView text;

        private OptionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
