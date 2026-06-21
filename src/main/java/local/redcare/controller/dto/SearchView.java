package local.redcare.controller.dto;

import local.redcare.domain.ScoredEntry;

import java.util.Collections;
import java.util.List;


public record SearchView(
        List<ScoredEntry> entries,
        int total,
        int page,
        int limit
) {

    public static SearchView of(List<ScoredEntry> entries, int page, int limit) {
        int total = entries.size();

        int startAt = Math.min(total - 1, (page - 1) * limit);
        int endAt = Math.min(total - 1, page * limit);

        return new SearchView(
                startAt == endAt ? Collections.emptyList() : entries.subList(startAt, endAt),
                total,
                page,
                limit
        );
    }
}
