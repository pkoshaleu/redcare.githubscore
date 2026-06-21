package local.redcare.service.handler;

import lombok.AllArgsConstructor;

import local.redcare.domain.ScoredEntry;
import local.redcare.domain.SearchRequest;
import local.redcare.domain.github.GitHubPage;
import local.redcare.domain.github.GitHubSearchEntry;
import local.redcare.service.GitHubService;
import local.redcare.service.SearchHandler;
import local.redcare.service.ScoreService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;


@AllArgsConstructor
public class DirectSearchHandler implements SearchHandler {

    private final GitHubService gitHubService;
    private final ScoreService scoreService;


    public List<ScoredEntry> invoke(SearchRequest request) {
        GitHubPage<GitHubSearchEntry> response = gitHubService.search(request, 1, 100);

        return response.getItems().stream()
                .map(it -> {
                    BigDecimal score = scoreService.score(it);
                    return mapWithScore(it, score);
                })
                .sorted(
                        Comparator.comparing(ScoredEntry::score)
                                .thenComparing(ScoredEntry::stars)
                                .thenComparing(ScoredEntry::forks)
                                .reversed()
                )
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
