package local.redcare.service.github;

import java.time.Instant;

public class BlockedException extends RuntimeException {

    private final transient Instant unblockAt;

    public BlockedException(Instant unblockAt) {
        super("GitHub API is blocked until " + unblockAt);
        this.unblockAt = unblockAt;
    }

    public Instant getUnblockAt() {
        return unblockAt;
    }

}
