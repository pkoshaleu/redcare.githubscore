package local.redcare.service;

import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.redcare.domain.SearchRequest;
import local.redcare.domain.github.GitHubPage;
import local.redcare.domain.github.GitHubQuery;
import local.redcare.domain.github.GitHubSearchEntry;


@Component
@AllArgsConstructor
public class GitHubService {

    private static final ParameterizedTypeReference<GitHubPage<GitHubSearchEntry>> REPO_SEARCH_PAGE
            = new ParameterizedTypeReference<>() {
    };

    private final RestClient githubRestClient;


    public GitHubPage<GitHubSearchEntry> search(SearchRequest request, int page, int limit) {
        String query = GitHubQuery.of(request);

        return githubRestClient.get().uri(builder ->
                builder.path("search/repositories")
                        .queryParam("q", query)
                        .queryParam("per_page", limit)
                        .queryParam("page", page)
                        .build()
        ).retrieve().body(REPO_SEARCH_PAGE);
    }

}
