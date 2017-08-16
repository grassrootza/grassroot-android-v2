package za.org.grassroot.android.services;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.RealmObject;
import za.org.grassroot.android.model.EntityForUpload;
import za.org.grassroot.android.model.UploadResult;

/**
 * Created by luke on 2017/08/16.
 */

public interface NetworkService {

    Observable<UploadResult> uploadEntity(EntityForUpload entityForUpload, boolean forceEvenIfPriorUploaded);

    // <E extends RealmObject> Single<E> downloadEntity(Class<E> clazz, String uid);

}
