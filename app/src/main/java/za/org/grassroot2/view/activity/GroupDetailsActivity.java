package za.org.grassroot2.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.presenter.GroupDetailsPresenter;
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter;
import za.org.grassroot2.view.dialog.AddMemberDialog;
import za.org.grassroot2.view.fragment.GroupTasksFragment;

public class GroupDetailsActivity extends GrassrootActivity implements GroupDetailsPresenter.GroupDetailsView {

    private static final String EXTRA_GROUP_UID       = "group_uid";
    private static final int REQUEST_PICK_CONTACTS = 1;

    @BindView(R.id.tabs)              TabLayout               tabs;
    @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.addPhoto)          ImageView               addPhoto;
    @BindView(R.id.toolbar)           Toolbar                 toolbar;
    @BindView(R.id.title)             TextView                title;
    @BindView(R.id.inviteMembers)     Button                  inviteMembers;
    @BindView(R.id.contentPager)      ViewPager               contentPager;
    @BindView(R.id.fab)               FloatingActionButton    fab;
    private                           String                  groupUid;

    @Inject GroupDetailsPresenter presenter;
    @Inject RxPermissions         rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupUid = getIntent().getStringExtra(EXTRA_GROUP_UID);
        initView();
        presenter.attach(this);
        presenter.init(groupUid);
        presenter.loadData();
    }

    @Override
    protected void onInject(ActivityComponent component) {
        super.onInject(component);
        component.inject(this);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_group_details;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach();
    }

    @OnClick(R.id.inviteMembers)
    void inviteMember() {
        displayInviteDialog();
    }

    private void displayInviteDialog() {
        AddMemberDialog df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_PICK);
        df.setAddMemberDialogListener(new AddMemberDialog.AddMemberDialogListener() {
            @Override
            public void contactBook() {
                rxPermissions.request(Manifest.permission.READ_CONTACTS).subscribe(result -> {
                    if (result) {
                        PickContactActivity.startForResult(GroupDetailsActivity.this, REQUEST_PICK_CONTACTS);
                    }
                }, Throwable::printStackTrace);
            }

            @Override
            public void manual() {
                showFillDialog();
            }
        });
        df.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    private void showFillDialog() {
        AddMemberDialog df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_INSERT_MANUAL);
        df.setContactListener((name, phone) -> presenter.inviteContact(name, phone));
        df.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    private void initView() {
        initTabs();
        initToolbar();
    }

    private void initTabs() {
        GenericViewPagerAdapter adapter = new GenericViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, null), getString(R.string.title_all));
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.VOTE), getString(R.string.title_votes));
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.MEETING), getString(R.string.title_meetings));
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.TODO), getString(R.string.title_todos));
        contentPager.setAdapter(adapter);
        tabs.setupWithViewPager(contentPager);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_group_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static void start(Activity activity, String groupUid) {
        Intent intent = new Intent(activity, GroupDetailsActivity.class);
        intent.putExtra(EXTRA_GROUP_UID, groupUid);
        activity.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_CONTACTS && resultCode == RESULT_OK) {
            List<Contact> contacts = (ArrayList<Contact>) data.getSerializableExtra(PickContactActivity.EXTRA_CONTACTS);
            presenter.inviteContacts(contacts);
        }
    }

    @Override
    public void render(Group group) {
        title.setText(group.getName());
    }

    @Override
    public void emptyData() {
//        ViewTooltip.on(inviteMembers)
//                .color(getResources().getColor(R.color.light_green))
//                .corner(10).autoHide(true, 2000).padding(10, 10, 10, 10)
//                .position(ViewTooltip.Position.TOP).text(R.string.info_invite_group_members)
//                .show();
        ViewTooltip.on(fab)
                .color(getResources().getColor(R.color.light_green))
                .corner(10).autoHide(true, 2000).padding(10, 10, 10, 10).align(ViewTooltip.ALIGN.CENTER)
                .position(ViewTooltip.Position.LEFT).text(R.string.info_group_create_item).setTextGravity(Gravity.CENTER)
                .show();
    }
}
