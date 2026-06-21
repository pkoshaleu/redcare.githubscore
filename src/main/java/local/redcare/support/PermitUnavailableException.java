package local.redcare.support;

public class PermitUnavailableException extends RuntimeException {

    public PermitUnavailableException(int permits) {
        super("No permits available; expected:" + permits);
    }

}
