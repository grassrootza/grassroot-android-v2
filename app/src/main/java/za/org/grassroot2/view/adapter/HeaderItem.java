package za.org.grassroot2.view.adapter;

import java.util.Date;

import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Task;

public class HeaderItem implements Task {

    public HeaderItem(String title) {
        headerName = title;
    }

    private String headerName;

    @Override
    public String getName() {
        return headerName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.GROUP;
    }

    @Override
    public String getUid() {
        return null;
    }

    @Override
    public long getLastTimeChangedServer() {
        return 0;
    }

    @Override
    public String getParentUid() {
        return null;
    }

    @Override
    public void setParentUid(String uid) {
    }

    @Override
    public void setUid(String uid) {
    }

    @Override
    public GrassrootEntityType getParentEntityType() {
        return null;
    }

    @Override
    public Date getCreatedDateTime() {
        return null;
    }

    @Override
    public long getDeadlineMillis() {
        return 0;
    }

    @Override
    public boolean hasResponded() {
        return false;
    }

    @Override
    public boolean hasMedia() {
        return false;
    }

    @Override
    public boolean isUserPartOf() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public long date() {
        return 0;
    }

    @Override
    public String searchableContent() {
        return null;
    }
}
