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
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.dto.ActionOption;
import za.org.grassroot2.model.util.GroupPermissionChecker;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.view.adapter.OptionAdapter;
import za.org.grassroot2.view.fragment.GrassrootFragment;

public class MediaPickerFragment extends GrassrootFragment {

    private static final String EXTRA_GROUP   = "group";
    private static final String EXTRA_OPTIONS = "options";
    @BindView(R.id.skip) Button       skip;
    @BindView(R.id.list) RecyclerView optionList;

    private PublishSubject<Integer> actionSubject = PublishSubject.create();
    private HashMap<Integer, ActionOption> options;

    public static MediaPickerFragment getMediaPicker() {
        HashMap<Integer, ActionOption> options = new LinkedHashMap<>();
        options.put(R.id.photo, new ActionOption(R.id.photo, R.string.take_photo, 0));
        options.put(R.id.video, new ActionOption(R.id.video, R.string.take_video, 0));
        options.put(R.id.gallery, new ActionOption(R.id.gallery, R.string.pick_gallery, 0));
        MediaPickerFragment f = new MediaPickerFragment();
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
        return R.layout.fragment_pick_media;
    }

    public Observable<Integer> clickAction() {
        return actionSubject;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        options = (HashMap<Integer, ActionOption>) getArguments().getSerializable(EXTRA_OPTIONS);
        initView();
        setupAdapter();
        RxView.clicks(skip).map(o -> skip.getId()).subscribe(actionSubject);
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