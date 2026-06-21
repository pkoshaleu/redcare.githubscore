package local.redcare.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;


public record ScoredEntry(

        Long repoId,
        String url,
        Integer stars,
        Integer forks,
        ZonedDateTime updatedAt,
        BigDecimal score

) {
}
