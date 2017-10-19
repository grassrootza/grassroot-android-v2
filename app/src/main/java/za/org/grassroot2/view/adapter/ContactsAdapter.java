package za.org.grassroot2.view.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.org.grassroot2.R;
import za.org.grassroot2.model.contact.Contact;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context       context;
    private       List<Contact> data;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public ContactsAdapter(Context c, List<Contact> data) {
        super();
        context = c;
        this.data = data;
    }

    public void update(List<Contact> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyDataSetChanged();
    }

    public List<Contact> getSelectedItems() {
        List<Contact> result = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            result.add(data.get(selectedItems.keyAt(i)));
        }
        return result;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Contact item = data.get(position);
        if (item != null) {
            ((ContactViewHolder) holder).bind(item, position);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)   View     root;
        @BindView(R.id.title)  TextView title;
        @BindView(R.id.number) TextView number;

        public ContactViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Contact c, int position) {
            title.setText(c.getDisplayName());
            if (!c.getPhoneNumbers().isEmpty()) {
                number.setText(c.getPhoneNumbers().get(0));
            }
            handleSelection(position);
            root.setOnClickListener(v -> toggleSelection(position));
        }

        private void handleSelection(int position) {
            if (selectedItems.get(position, false)) {
                root.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryTransparent));
            } else {
                root.setBackgroundColor(0);
            }
        }
    }
}