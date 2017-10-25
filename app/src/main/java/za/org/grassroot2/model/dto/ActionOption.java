package za.org.grassroot2.model.dto;

import java.io.Serializable;

public class ActionOption implements Serializable {
    public final int id;
    public final int textId;
    public final int resId;

    public ActionOption(int id, int textId, int resId) {
        this.id = id;
        this.textId = textId;
        this.resId = resId;
    }
}
