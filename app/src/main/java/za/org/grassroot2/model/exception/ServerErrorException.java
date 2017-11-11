package za.org.grassroot2.model.exception;

/**
 * Created by luke on 2017/08/16.
 */

public class ServerErrorException extends RuntimeException {
    public final int code;

    public ServerErrorException(int code) {
        this.code = code;
    }
}
