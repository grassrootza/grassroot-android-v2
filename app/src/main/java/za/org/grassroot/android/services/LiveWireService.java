package za.org.grassroot.android.services;

import io.reactivex.Single;

/**
 * Created by luke on 2017/08/15.
 * Use this service to create and store LiveWire alerts
 */

public interface LiveWireService {

    /**
     *
     * @return The UID of the alert entity
     */
    Single<String> initiateAlertWithMedia(String mediaFileUid);

    Single<String> initiateAlertWithHeadline(String headline);

    Single<Boolean> updateAlertMedia(String alertUid, String mediaFileUid);

    Single<Boolean> updateAlertHeadline(String alertUid, String headline);

    Single<Boolean> updateAlertDescription(String alertUid, String description);

    Single<Boolean> updateAlertGroupUid(String alertUid, String groupUid);

    Single<Boolean> updateAlertTaskUid(String alertUid, String taskUid, String taskType);

    Single<Boolean> markAlertReadyForReview(String alertUid);

}
