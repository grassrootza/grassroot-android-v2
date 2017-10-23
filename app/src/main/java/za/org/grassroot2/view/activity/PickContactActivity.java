package za.org.grassroot2.view.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.presenter.PickContactPresenter;
import za.org.grassroot2.view.adapter.ContactsAdapter;

public class PickContactActivity extends GrassrootActivity implements PickContactPresenter.PickContactView {

    public static final String EXTRA_CONTACTS = "contacts";
    @BindView(R.id.contactList) RecyclerView contactList;
    @BindView(R.id.toolbar)     Toolbar      toolbar;
    @BindView(R.id.emptyInfo)   TextView     emptyInfo;

    @Inject PickContactPresenter presenter;
    private ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        presenter.attach(this);
        contactList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(this, new ArrayList<>());
        contactList.setAdapter(adapter);
        presenter.loadContacts();
        setSupportActionBar(toolbar);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_pick_contact;
    }

    @Override
    protected void onInject(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void render(List<Contact> contacts) {
        adapter.update(contacts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pick_contact, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                List<Long> selectedItems = adapter.getSelectedItems();
                if (!selectedItems.isEmpty()) {
                    List<Contact> contacts = presenter.loadContactsForIds(selectedItems);
                    Intent data = new Intent();
                    data.putExtra(EXTRA_CONTACTS, (ArrayList<Contact>)contacts);
                    setResult(RESULT_OK, data);
                    finish();
                }
                return true;
        }
    }

    @Override
    public void emptyData() {
        emptyInfo.setVisibility(View.VISIBLE);
        emptyInfo.setText(R.string.no_contacts);
    }

    public static void startForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, PickContactActivity.class), requestCode);
    }
}
