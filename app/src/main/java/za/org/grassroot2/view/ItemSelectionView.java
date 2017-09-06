package za.org.grassroot2.view;

import io.reactivex.Observable;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import za.org.grassroot2.model.SelectableItem;

/**
 * Created by luke on 2017/08/19.
 */

public interface ItemSelectionView<T extends RealmObject & SelectableItem> {

    Observable<Boolean> viewAttached();
    Observable<String> addData(OrderedRealmCollection<T> data);

}
