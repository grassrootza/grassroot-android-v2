package za.org.grassroot2.services

import za.org.grassroot2.services.Status.*

class Resource<T> private constructor(val status: Status, val data: T?, val message: String?) {
    companion object {

        fun <T> success(data: T): Resource<T> = Resource(SUCCESS, data, null)
        fun <T> error(msg: String, data: T?) = Resource(ERROR, data, msg)
        fun <T> serverError(msg: String, data: T?)= Resource(SERVER_ERROR, data, msg)
        fun <T> loading(data: T?): Resource<T> = Resource(LOADING, data, null)

    }
}
