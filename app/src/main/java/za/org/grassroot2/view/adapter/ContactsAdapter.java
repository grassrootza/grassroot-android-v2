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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.util.ContactHelper;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final Context       context;
    private       List<Contact> data;
    private List<Contact> contactListFiltered;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public ContactsAdapter(Context c, List<Contact> data) {
        super();
        context = c;
        this.data = data;
        this.contactListFiltered = data;
    }

    public void update(List<Contact> data) {
        this.data = data;
        this.contactListFiltered = data;
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

    public List<Long> getSelectedItems() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            result.add(data.get(selectedItems.keyAt(i)).getId());
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
        Contact item = contactListFiltered.get(position);
        if (item != null) {
            ((ContactViewHolder) holder).bind(item, position);
        }
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if(charString.isEmpty()){
                    contactListFiltered = data;
                }else{
                    List<Contact> filteredList = new ArrayList<>();
                    for(Contact contact:data){
                        if(contact.getDisplayName().toLowerCase().contains(charString.toLowerCase())){
                            filteredList.add(contact);
                        }
                    }
                    contactListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                contactListFiltered = (ArrayList<Contact>) results.values;
                notifyDataSetChanged();
            }
        };
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
