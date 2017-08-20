package za.org.grassroot.android.view;

import io.reactivex.Observable;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import za.org.grassroot.android.model.SelectableItem;

/**
 * Created by luke on 2017/08/19.
 */

public interface ItemSelectionView<T extends RealmObject & SelectableItem> extends ProgressBarContainer {

    Observable<Boolean> viewAttached();
    Observable<String> addData(OrderedRealmCollection<T> data);

}
