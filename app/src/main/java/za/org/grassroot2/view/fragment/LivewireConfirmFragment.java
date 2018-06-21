package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.alert.LiveWireAlert;

public class LivewireConfirmFragment extends GrassrootFragment {

    private static final String EXTRA_HEADLINE   = "date";
    private static final String EXTRA_HAS_MEDIA  = "has_media";
    private static final String EXTRA_GROUP_NAME = "group_name";
    @BindView(R.id.desc) TextView desc;
    @BindView(R.id.next) View     next;

    private PublishSubject<Boolean> actionSubject = PublishSubject.create();
    private BackNavigationListener listener;
    private String                 headline;

    public static LivewireConfirmFragment newInstance(LiveWireAlert alert, String groupName) {
        LivewireConfirmFragment f = new LivewireConfirmFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_HEADLINE, alert.getHeadline());
        b.putBoolean(EXTRA_HAS_MEDIA, alert.getMediaFile() != null);
        b.putString(EXTRA_GROUP_NAME, groupName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (BackNavigationListener) getActivity();
    }

    @OnClick({R.id.backNav, R.id.cancel})
    void back() {
        listener.backPressedAndRemoveLast();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        headline = getArguments().getString(EXTRA_HEADLINE);
        boolean hasFile = getArguments().getBoolean(EXTRA_HAS_MEDIA);
        String groupName = getArguments().getString(EXTRA_GROUP_NAME);
        desc.setText(hasFile ? getString(R.string.lwire_confirm_text_media, headline, groupName) : getString(R.string.lwire_confirm_text_no_media, headline, groupName));
        RxView.clicks(next).map(o -> true).subscribe(actionSubject);
    }

    public Observable<Boolean> livewireAlertConfirmed() {
        return actionSubject;
    }

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_livewire_confirm;
    }

}
