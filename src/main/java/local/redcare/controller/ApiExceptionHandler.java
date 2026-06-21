package local.redcare.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import local.redcare.service.TimeService;
import local.redcare.service.github.BlockedException;
import local.redcare.service.github.QuotaGateException;
import local.redcare.support.PermitUnavailableException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final TimeService timeService;

    @ExceptionHandler(BlockedException.class)
    public ResponseEntity<ProblemDetail> handleBlockedEx(BlockedException ex) {
        long retryAfter = Math.max(0, Duration.between(timeService.now(), ex.getUnblockAt()).toSeconds());

        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        body.setTitle("Upstream rate limited");
        body.setProperty("retryAfterSeconds", retryAfter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter));
        return new ResponseEntity<>(body, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler({PermitUnavailableException.class, QuotaGateException.class})
    public ProblemDetail handleBackpressureEx(RuntimeException ex) {
        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        body.setTitle("Service busy");
        return body;
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail handleUpstreamEx(RestClientResponseException ex) {
        log.warn("GitHub responded {}: {}", ex.getStatusCode(), ex.getMessage());
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "GitHub API returned an error.");
        body.setTitle("Upstream error");
        return body;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedEx(Exception ex) {
        log.error("Unexpected error handling request", ex);
        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        body.setTitle("Internal error");
        return body;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request validation failed.");
        body.setTitle("Invalid request");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

}
