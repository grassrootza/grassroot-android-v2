package za.org.grassroot.android.model;

import com.google.gson.annotations.SerializedName;

public class Group {

    @SerializedName("groupUid")
    private String uid;

    @SerializedName("groupName")
    private String name;

    @SerializedName("groupCreator")
    private String creatorName;
}
