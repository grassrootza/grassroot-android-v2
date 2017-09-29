package za.org.grassroot2.services.rest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SyncResponse<T> {

    private T addedAndUpdated;

    @SerializedName("deleted")
    private List<String> deletedUids;

    public T getAddedAndUpdated() {
        return addedAndUpdated;
    }

    public void setAddedAndUpdated(T addedAndUpdated) {
        this.addedAndUpdated = addedAndUpdated;
    }

    public List<String> getDeletedUids() {
        return deletedUids;
    }

    public void setDeletedUids(List<String> deletedUids) {
        this.deletedUids = deletedUids;
    }
}
