package za.org.grassroot.android.model;

import io.realm.RealmObject;

/**
 * Created by luke on 2017/07/06.
 */
public class UserDetails extends RealmObject {

    private String uid;

    private String phoneNumber;
    private String displayName;

}
