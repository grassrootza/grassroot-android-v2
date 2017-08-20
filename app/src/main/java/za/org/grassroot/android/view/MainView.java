package za.org.grassroot.android.view;

import io.reactivex.Observable;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import za.org.grassroot.android.model.SelectableItem;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnReturnBundle;

public interface MainView extends LoggedInView {

    Observable<CharSequence> mainTextNext();

    void setBtnGrouping(BtnGrouping btnGrouping);
    Observable<BtnReturnBundle> threeButtonRowButtonClicked();

    <T extends RealmObject & SelectableItem> Observable<String> requestSelection(int headerRes,
                                                                                 OrderedRealmCollection<T> items);

}