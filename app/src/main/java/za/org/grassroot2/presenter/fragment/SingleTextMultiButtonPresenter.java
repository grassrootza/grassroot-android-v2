package za.org.grassroot2.presenter.fragment;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import za.org.grassroot2.view.FragmentView;
import za.org.grassroot2.view.event.MoveNextWithInputEvent;


public class SingleTextMultiButtonPresenter extends BaseFragmentPresenter<SingleTextMultiButtonPresenter.SingleTextMultiButtonView> {

    @Override
    public void onViewCreated() {
        disposableOnDetach(getView().inputTextDone().subscribe(charSequence -> {
            EventBus.getDefault().post(new MoveNextWithInputEvent(charSequence.toString()));
        }, Throwable::printStackTrace));
    }

    public interface SingleTextMultiButtonView extends FragmentView {
        Observable<CharSequence> inputTextDone();
        void setupButtons();
    }
}
