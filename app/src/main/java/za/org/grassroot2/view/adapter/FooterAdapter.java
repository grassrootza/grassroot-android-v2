package za.org.grassroot2.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class FooterAdapter<E> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<E> items = new ArrayList<>();
    List<View> footers = new ArrayList<>();

    public static final int TYPE_FOOTER = 222;
    public static final int TYPE_ITEM   = 333;

    public FooterAdapter(List<E> data) {
        items = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        if (type == TYPE_FOOTER) {
            FrameLayout frameLayout = new FrameLayout(viewGroup.getContext());
            //make sure it fills the space
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new FooterViewHolder(frameLayout);
        }
        return null;
    }

    protected abstract void bindRegularViewHolder(RecyclerView.ViewHolder vh, int position);

    protected void prepareFooter(FooterViewHolder vh, View v) {
        vh.base.removeAllViews();
        vh.base.addView(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder vh, int position) {
        if (position >= items.size()) {
            View v = footers.get(position - items.size());
            prepareFooter((FooterViewHolder) vh, v);
        } else {
            bindRegularViewHolder(vh, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + footers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= items.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    public void addFooter(View footer) {
        if (!footers.contains(footer)) {
            footers.add(footer);
            notifyItemInserted(items.size() + footers.size() - 1);
        }
    }

    public void removeFooter(View footer) {
        if (footers.contains(footer)) {
            notifyItemRemoved(items.size() + footers.indexOf(footer));
            footers.remove(footer);
            if (footer.getParent() != null) {
                ((ViewGroup) footer.getParent()).removeView(footer);
            }
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        FrameLayout base;

        public FooterViewHolder(View itemView) {
            super(itemView);
            this.base = (FrameLayout) itemView;
        }
    }
}