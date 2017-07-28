package za.org.grassroot.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by luke on 2017/07/28.
 */

public class Group {

    @SerializedName("groupUid")
    private String uid;
    @SerializedName("groupName")
    private String name;
    @SerializedName("groupCreator")
    private String creatorName;



}
