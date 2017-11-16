package za.org.grassroot2.services.rest;

import com.google.gson.annotations.SerializedName;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class RestResponse<T> {

    protected String status;
    protected Integer code;
    protected String message;

    @SerializedName("data")
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static Response<Void> errorResponse() {
        return Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), ""));
    }
}
