package za.org.grassroot2.model.util

import za.org.grassroot2.model.Group
import za.org.grassroot2.model.GroupPermission

/**
 * Created by qbasso on 20.10.2017.
 */

object GroupPermissionChecker {

    fun hasCreatePermission(g: Group): Boolean {
        return g.permissions.contains(GroupPermission.CREATE_GROUP_MEETING) ||
                g.permissions.contains(GroupPermission.CREATE_GROUP_ENTRY) ||
                g.permissions.contains(GroupPermission.CREATE_GROUP_VOTE)
    }

    fun canCallMeeting(g: Group?): Boolean {
        return g?.permissions?.contains(GroupPermission.CREATE_GROUP_MEETING) ?: false
    }

    fun canCreateVote(g: Group?): Boolean {
        return g?.permissions?.contains(GroupPermission.CREATE_GROUP_VOTE) ?: false
    }

    fun canCreateTodo(g: Group?): Boolean {
        return g?.permissions?.contains(GroupPermission.CREATE_GROUP_ENTRY) ?: false
    }
}
