package gg.sunken.sdk.exceptions;

public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        super("This is not implemented yet.");
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
