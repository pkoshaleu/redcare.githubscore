package local.redcare.service.github;

public class QuotaGateException extends RuntimeException {

    public QuotaGateException(String message, Throwable cause) {
        super(message, cause);
    }

}
