package pl.cleankod.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.cleankod.model.exception.ApiError;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler({
            CurrencyConversionException.class,
            IllegalArgumentException.class
    })
    protected ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        int status = 400;
        String message = ex.getMessage();
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            message += " | Cause: " + cause.getMessage();
        }
        return ResponseEntity.status(status).body(new ApiError(message));
    }

    @ExceptionHandler(NbpApiException.class)
    protected ResponseEntity<ApiError> handleNbpApiException(NbpApiException ex) {
        int status = 503;
        Throwable cause = ex.getCause();
        if (cause instanceof feign.FeignException fe) {
            status = fe.status() > 0 ? fe.status() : 503;
        }
        String message = ex.getMessage();
        if (cause != null && cause.getMessage() != null) {
            message += " | Cause: " + cause.getMessage();
        }
        return ResponseEntity.status(status).body(new ApiError(message));
    }

}
