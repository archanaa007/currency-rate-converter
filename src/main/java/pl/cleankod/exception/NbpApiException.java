package pl.cleankod.exception;

public class NbpApiException extends RuntimeException {
    public NbpApiException(String message, Throwable cause) {
        super(message, cause);
    }
    public NbpApiException(String message) {
        super(message);
    }
}

