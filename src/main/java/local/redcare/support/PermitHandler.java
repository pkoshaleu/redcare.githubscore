package local.redcare.support;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class PermitHandler {

    private final Semaphore semaphore;

    public PermitHandler(int maxPermits) {
        semaphore = new Semaphore(maxPermits, true);
    }

    public Permit enter(int permits) throws InterruptedException {
        return new Permit(permits);
    }

    public class Permit implements AutoCloseable {

        private final AtomicBoolean isClosed = new AtomicBoolean(false);
        private final int permits;

        private Permit(int permits) throws InterruptedException {
            if (semaphore.tryAcquire(permits, 15, TimeUnit.SECONDS)) {
                this.permits = permits;
            } else {
                throw new InterruptedException("No permits available");
            }
        }

        @Override
        public void close() {
            if (isClosed.compareAndSet(false, true)) {
                semaphore.release(permits);
            }
        }
    }

}
