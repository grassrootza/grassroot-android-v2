package za.org.grassroot2.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.dto.ActionOption;
import za.org.grassroot2.model.util.GroupPermissionChecker;
import za.org.grassroot2.view.adapter.OptionAdapter;
import za.org.grassroot2.view.fragment.GrassrootFragment;

public class MultiOptionPickFragment extends GrassrootFragment {

    private static final String EXTRA_GROUP                = "group";
    private static final String EXTRA_OPTIONS              = "options";
    @BindView(R.id.close) Button       close;
    @BindView(R.id.list)  RecyclerView optionList;

    private PublishSubject<Integer> actionSubject = PublishSubject.create();
    private HashMap<Integer, ActionOption> options;

    public static MultiOptionPickFragment getActionPicker(Group group) {
        HashMap<Integer, ActionOption> options = new LinkedHashMap<>();
        options.put(R.id.dictate, new ActionOption(R.id.dictate, R.string.dictate_your_action, R.drawable.ic_mic_24dp));
        options.put(R.id.createLivewireAlert, new ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_mic_24dp));
        options.put(R.id.createTodo, new ActionOption(R.id.createTodo, R.string.create_to_do, R.drawable.ic_format_list_bulleted_24dp));
        options.put(R.id.takeVote, new ActionOption(R.id.takeVote, R.string.take_a_vote, R.drawable.ic_mic_24dp));
        options.put(R.id.callMeeting, new ActionOption(R.id.callMeeting, R.string.call_a_meeting, R.drawable.ic_date_range_green_24dp));
        MultiOptionPickFragment f = new MultiOptionPickFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_OPTIONS, options);
        b.putSerializable(EXTRA_GROUP, group);
        f.setArguments(b);
        return f;
    }

    public static MultiOptionPickFragment getHomeActionPicker() {
        HashMap<Integer, ActionOption> options = new LinkedHashMap<>();
        options.put(R.id.createGroup, new ActionOption(R.id.createGroup, R.string.createGroup, R.drawable.ic_mic_24dp));
        options.put(R.id.takeAction, new ActionOption(R.id.takeAction, R.string.takeAction, R.drawable.ic_mic_24dp));
        options.put(R.id.dictate, new ActionOption(R.id.dictate, R.string.dictateMyAction, R.drawable.ic_date_range_green_24dp));
        MultiOptionPickFragment f = new MultiOptionPickFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_OPTIONS, options);
        f.setArguments(b);
        return f;
    }

    public static MultiOptionPickFragment homeTakeActionFragment() {
        HashMap<Integer, ActionOption> options = new LinkedHashMap<>();
        options.put(R.id.createLivewireAlert, new ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_mic_24dp));
        options.put(R.id.createTodo, new ActionOption(R.id.createTodo, R.string.create_to_do, R.drawable.ic_format_list_bulleted_24dp));
        options.put(R.id.takeVote, new ActionOption(R.id.takeVote, R.string.take_a_vote, R.drawable.ic_mic_24dp));
        options.put(R.id.callMeeting, new ActionOption(R.id.callMeeting, R.string.call_a_meeting, R.drawable.ic_date_range_green_24dp));
        MultiOptionPickFragment f = new MultiOptionPickFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_OPTIONS, options);
        f.setArguments(b);
        return f;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_multi_option_pick;
    }

    public Observable<Integer> clickAction() {
        return actionSubject;
    }

    @OnClick(R.id.close)
    void closeClick() {
        actionSubject.onComplete();
        getActivity().finish();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        options = (HashMap<Integer, ActionOption>) getArguments().getSerializable(EXTRA_OPTIONS);
        initView();
        setupAdapter();
        return v;
    }

    private void setupAdapter() {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.item_divider));
        optionList.addItemDecoration(itemDecoration);
        optionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        OptionAdapter adapter = new OptionAdapter(new ArrayList<>(options.values()));
        adapter.getViewClickObservable().subscribe(actionSubject);
        optionList.setAdapter(adapter);
    }

    private void initView() {
        Group g = (Group) getArguments().getSerializable(EXTRA_GROUP);
        if (g != null) {
            if (!GroupPermissionChecker.canCallMeeting(g)) {
                options.remove(R.id.callMeeting);
            }
            if (!GroupPermissionChecker.canCreateTodo(g)) {
                options.remove(R.id.createTodo);
            }
            if (!GroupPermissionChecker.canCreateVote(g)) {
                options.remove(R.id.takeVote);
            }
        }
    }

}