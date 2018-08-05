package za.org.grassroot2.view.adapter

import java.util.Date

import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Task

class HeaderItem(private val headerName: String) : Task {

    override fun getName(): String {
        return headerName
    }

    override fun getDescription(): String? {
        return null
    }

    override fun getType(): GrassrootEntityType {
        return GrassrootEntityType.GROUP
    }

    override fun getUid(): String? {
        return null
    }

    override fun getLastTimeChangedServer(): Long {
        return 0
    }

    override fun getParentUid(): String? {
        return null
    }

    override fun setParentUid(uid: String) {}

    override fun setUid(uid: String) {}

    override fun getParentEntityType(): GrassrootEntityType? {
        return null
    }

    override fun getCreatedDateTime(): Date? {
        return null
    }

    override fun getDeadlineMillis(): Long {
        return 0
    }

    override fun hasResponded(): Boolean {
        return false
    }

    override fun hasMedia(): Boolean {
        return false
    }

    override fun isUserPartOf(): Boolean {
        return false
    }

    override fun isPublic(): Boolean {
        return false
    }

    override fun date(): Long {
        return 0
    }

    override fun searchableContent(): String? {
        return null
    }
}
