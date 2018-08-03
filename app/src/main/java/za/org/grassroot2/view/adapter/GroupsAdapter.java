package za.org.grassroot2.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.util.LastModifiedFormatter;

public class GroupsAdapter extends FooterEnabledAdapter<Group> {

    private final Context context;
    private PublishSubject<String> viewClickSubject = PublishSubject.create();

    private PublishSubject<String> groupImageClickSubject = PublishSubject.create();

    public Observable<String> getViewClickObservable() {
        return viewClickSubject;
    }

    public Observable<String> getGroupImageClickObservable() {return groupImageClickSubject; }

    public GroupsAdapter(Context c, List<Group> data) {
        super(data);
        context = c;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder v = super.onCreateViewHolder(parent, viewType);
        if (v == null) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);

            return new GroupViewHolder(view);
        }

        return v;
    }

    @Override
    protected void bindRegularViewHolder(RecyclerView.ViewHolder vh, int position) {
        Group item = items.get(position);
        GroupViewHolder holder = (GroupViewHolder) vh;

        if (item != null) {
            holder.name.setText(item.getName());
            holder.count.setText(holder.count.getContext().getResources().getQuantityString(R.plurals.member_count, item.getMemberCount(), item.getMemberCount()));
            holder.lastModified.setText(LastModifiedFormatter.lastSeen(context, item.getLastTimeChangedServer()));
            holder.organiser.setText("Placeholder");
            holder.letter.setText(item.getName().substring(0, 1));
            holder.letter.setVisibility(View.VISIBLE);

            if(item.getProfileImageUrl() != null){
                Picasso.get()
                        .load(item.getProfileImageUrl())
                        .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                        .centerCrop()
                        .into(holder.image);
            }

            RxView.clicks(holder.root)
                    .map(o -> item.getUid())
                    .subscribe(viewClickSubject);

            RxView.clicks(holder.image)
                    .map(object -> item.getUid())
                    .subscribe(groupImageClickSubject);

        }
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)         View     root;
        @BindView(R.id.letter)       TextView letter;
        @BindView(R.id.name)         TextView name;
        @BindView(R.id.organiser)    TextView organiser;
        @BindView(R.id.count)        TextView count;
        @BindView(R.id.lastModified) TextView lastModified;
        @BindView(R.id.image) ImageView image;

        private GroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
