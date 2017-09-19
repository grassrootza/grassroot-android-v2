package za.org.grassroot2.view;

import java.util.List;

import io.reactivex.Observable;
import za.org.grassroot2.model.SelectableItem;

/**
 * Created by luke on 2017/08/19.
 */

public interface ItemSelectionView<T extends SelectableItem> {

    Observable<Boolean> viewAttached();
    Observable<String> addData(List<T> data);

}
