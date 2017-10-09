package za.org.grassroot2.view.activity;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.org.grassroot2.R;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.presenter.GroupDetailsPresenter;
import za.org.grassroot2.presenter.fragment.GroupTasksPresenter;
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter;
import za.org.grassroot2.view.fragment.GroupTasksFragment;

public class GroupDetailsActivity extends GrassrootActivity implements GroupDetailsPresenter.GroupDetailsView {

    private static final String EXTRA_GROUP_UID = "group_uid";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        ButterKnife.bind(this);
        getAppComponent().plus(getActivityModule()).inject(this);
        groupUid = getIntent().getStringExtra(EXTRA_GROUP_UID);
        initView();
        presenter.attach(this);
        presenter.loadData(groupUid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
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
