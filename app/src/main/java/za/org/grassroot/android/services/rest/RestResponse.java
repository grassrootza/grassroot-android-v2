package za.org.grassroot.android.services.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by luke on 2017/07/16.
 */
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
}
