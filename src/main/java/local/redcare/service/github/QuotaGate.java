package local.redcare.service.github;

import lombok.extern.slf4j.Slf4j;

import local.redcare.support.PermitHandler;

import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class QuotaGate {

    public enum Mode {

        FULL_OPEN(1),
        HALF_OPEN(2),
        DRIPPING(4);

        private final int permits;

        Mode(int permits) {
            this.permits = permits;
        }

    }

    private static final long DEFAULT_QUOTA = 500;

    private final AtomicReference<Mode> mode = new AtomicReference<>(Mode.FULL_OPEN);
    private final PermitHandler guard = new PermitHandler(4);

    public PermitHandler.Permit enter() throws InterruptedException {
        int permits = mode.get().permits;
        log.debug("Entering gate; permits={}", permits);
        return guard.enter(permits);
    }

    public void observe(Long used, Long limit) {
        long promille = (used == null || limit == null) ? DEFAULT_QUOTA : 1000 * used / limit;
        updateQuota(promille);
    }

    public void updateQuota(long quota) {
        switch (mode.get()) {
            case FULL_OPEN -> {
                if (quota > 750) {
                    mode.compareAndSet(Mode.FULL_OPEN, Mode.HALF_OPEN);
                }
            }
            case HALF_OPEN -> {
                if (quota < 650) {
                    mode.compareAndSet(Mode.HALF_OPEN, Mode.FULL_OPEN);
                } else if (quota > 900) {
                    mode.compareAndSet(Mode.HALF_OPEN, Mode.DRIPPING);
                }
            }
            case DRIPPING -> {
                if (quota < 850) {
                    mode.compareAndSet(Mode.DRIPPING, Mode.HALF_OPEN);
                }
            }
        }

        log.debug("Updating gate quota; quota={}, mode={}", quota, mode.get());
    }

    public Mode getMode() {
        return mode.get();
    }

}
