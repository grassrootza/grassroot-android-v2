package za.org.grassroot2.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static za.org.grassroot2.services.Status.ERROR;
import static za.org.grassroot2.services.Status.LOADING;
import static za.org.grassroot2.services.Status.SERVER_ERROR;
import static za.org.grassroot2.services.Status.SUCCESS;

public class Resource<T> {
    @NonNull public final Status status;
    @Nullable public final T data;
    @Nullable public final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(ERROR, data, msg);
    }

    public static <T> Resource<T> serverError(String msg, @Nullable T data) {
        return new Resource<>(SERVER_ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null);
    }
}
