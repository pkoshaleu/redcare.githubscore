package local.redcare.service;

import jakarta.validation.ValidationException;
import local.redcare.domain.github.GitHubPage;
import local.redcare.domain.github.GitHubSearchEntry;
import local.redcare.domain.github.SearchRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class GitHubService {

    private static final ParameterizedTypeReference<GitHubPage<GitHubSearchEntry>> REPO_SEARCH_PAGE
            = new ParameterizedTypeReference<>() {};

    private final RestClient githubRestClient;


    public GitHubPage<GitHubSearchEntry> search(SearchRequest request) {
        String query = toQuery(request);

        if (query.length() >= 255) {
            throw new ValidationException("Too long query");
        }

        return githubRestClient.get().uri(builder ->
                builder.path("search/repositories")
                        .queryParam("q", query)
                        //TODO: load more case - expand to the limits
                        .queryParam("per_page", 100)
                        .queryParam("page", request.page())
                        .build()
        ).retrieve().body(REPO_SEARCH_PAGE);
    }

    public static String toQuery(SearchRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.q());
        if (request.lang() != null) {
            sb.append(" lang:").append(request.lang());
        }
        if (request.since() != null) {
            sb.append(" created:>").append(request.since());
        }

        return sb.toString();
    }

}
