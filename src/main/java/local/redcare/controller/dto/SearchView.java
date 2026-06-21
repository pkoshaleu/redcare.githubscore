package local.redcare.controller.dto;

import local.redcare.domain.ScoredEntry;

import java.util.List;


public record SearchView(
        List<ScoredEntry> entries,
        int total,
        int page,
        int limit
) {

    public static SearchView of(List<ScoredEntry> entries, int page, int limit) {
        int total = entries.size();
        int from = (page - 1) * limit;
        int start = Math.min(from, total);
        int end = Math.min(from + limit, total);

        return new SearchView(
                start >= end ? List.of() : entries.subList(start, end),
                total,
                page,
                limit
        );
    }
}
