package local.redcare.service;

import local.redcare.domain.ScoredEntry;
import local.redcare.github.GitHubPage;
import local.redcare.github.GitHubSearchEntry;
import local.redcare.github.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;


@Component
@AllArgsConstructor
public class DirectSearchHandler {

    private final GitHubService gitHubService;
    private final ScoreService scoreService;


    public List<ScoredEntry> invoke(SearchRequest request) {
        GitHubPage<GitHubSearchEntry> response = gitHubService.search(request);

        return response.getItems().stream()
                .map(it -> {
                    BigDecimal score = scoreService.score(it);
                    return mapWithScore(it, score);
                })
                .sorted(Comparator.comparing(ScoredEntry::score).reversed())
                //TODO: load more case - limit output to the expected
                .limit(request.limit())
                .toList();
    }

    public ScoredEntry mapWithScore(GitHubSearchEntry entry, BigDecimal score) {
        return new ScoredEntry(
                entry.getId(),
                entry.getHtmlUrl(),
                entry.getStargazersCount(),
                entry.getForksCount(),
                entry.getPushedAt(),
                score
        );
    }

}
