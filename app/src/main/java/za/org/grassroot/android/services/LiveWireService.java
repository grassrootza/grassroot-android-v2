package za.org.grassroot.android.services;

import io.reactivex.Observable;
import io.reactivex.Single;
import za.org.grassroot.android.model.LiveWireAlert;

/**
 * Created by luke on 2017/08/15.
 * Use this service to create and store LiveWire alerts
 */

public interface LiveWireService {

    /**
     *
     * @return The UID of the alert entity
     */
    Single<LiveWireAlert> load(String alertUid);

    Single<String> initiateAlertWithMedia(String mediaFileUid);

    Single<String> initiateAlertWithHeadline(String headline);

    Single<Boolean> updateAlertMedia(String alertUid, String mediaFileUid);

    Single<String> updateAlertHeadline(String alertUid, String headline);

    Single<Boolean> updateAlertDescription(String alertUid, String description);

    Single<Boolean> setGenericAlert(String alertUid, String groupUid);

    Single<Boolean> setMeetingAlert(String alertUid, String meetingUid);

    Single<Boolean> markAlertReadyForDispatch(String alertUid);

    // returns one by if there are media files that have to be uploaded first
    Observable<String> triggerAlertDispatch(String alertUid);

}
