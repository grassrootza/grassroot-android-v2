package za.org.grassroot2.model.request;

import za.org.grassroot2.model.network.EntityForDownload;

/**
 * Created by luke on 2017/09/20.
 */

public interface Request extends EntityForDownload {

    // corresponds to enum on server platform, different states of a request
    String PENDING = "PENDING";
    String APPROVED = "APPROVED";
    String DECLINED = "DECLINED";
    String CANCELLED = "CANCELLED";
    String CLOSED = "CLOSED";

    String getStatus();
    boolean isThisUserGenerated();
    boolean canThisUserApprove();

}
