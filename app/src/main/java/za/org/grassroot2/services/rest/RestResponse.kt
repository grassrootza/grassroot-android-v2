package za.org.grassroot2.services.rest

import com.google.gson.annotations.SerializedName

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response

class RestResponse<T> {

    var status: String = ""
    var code: Int? = null
    var message: String = ""

    @SerializedName("data")
    var data: T? = null

    companion object {

        fun errorResponse(): Response<Void> {
            return Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), ""))
        }
    }
}
