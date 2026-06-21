package local.redcare.github;

import java.time.LocalDate;

public record SearchRequest (

        String q,
        LocalDate since,
        String lang,
        int page,
        int limit

) {
}
