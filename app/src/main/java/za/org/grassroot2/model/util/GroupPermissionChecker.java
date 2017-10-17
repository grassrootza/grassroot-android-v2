package za.org.grassroot2.model.util;

import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.GroupPermission;

/**
 * Created by qbasso on 20.10.2017.
 */

public class GroupPermissionChecker {

    public static boolean hasCreatePermission(Group g) {
        return g.getPermissions().contains(GroupPermission.CREATE_GROUP_MEETING) ||
                g.getPermissions().contains(GroupPermission.CREATE_GROUP_ENTRY) ||
                g.getPermissions().contains(GroupPermission.CREATE_GROUP_VOTE);
    }

    public static boolean canCallMeeting(Group g) {
        return g.getPermissions().contains(GroupPermission.CREATE_GROUP_MEETING);
    }

    public static boolean canCreateVote(Group g) {
        return g.getPermissions().contains(GroupPermission.CREATE_GROUP_VOTE);
    }

    public static boolean canCreateTodo(Group g) {
        return g.getPermissions().contains(GroupPermission.CREATE_GROUP_ENTRY);
    }
}
